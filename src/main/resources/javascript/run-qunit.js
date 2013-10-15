/*
 run-qunit (from the PhantomJS examples, with the addition of `page.evaluateFailure` by Jason Bertsche)
 Copyright (c) 2011, Ariya Hidayat
 All rights reserved.
*/

var system = require('system');

/**
 * Wait until the test condition is true or a timeout occurs. Useful for waiting
 * on a server response or for a ui change (fadeIn, etc.) to occur.
 *
 * @param testFx javascript condition that evaluates to a boolean,
 * it can be passed in as a string (e.g.: "1 == 1" or "$('#bar').is(':visible')" or
 * as a callback function.
 * @param onReady what to do when testFx condition is fulfilled,
 * it can be passed in as a string (e.g.: "1 == 1" or "$('#bar').is(':visible')" or
 * as a callback function.
 * @param timeOutMs the max amount of time to wait. If not specified, 3 sec is used.
 */
function waitFor(testFx, onReady, timeOutMs) {

  var cond = false;

  var interval = setInterval(function() {

    var maxMs     = timeOutMs ? timeOutMs : 3001;
    var startTime = new Date().getTime();

    // If not time-out yet and condition not yet fulfilled
    if ((new Date().getTime() - startTime < maxMs) && !cond) {
      cond = (typeof(testFx) === "string" ? eval(testFx) : testFx()); // defensive code
    } else {
      if(!cond) {
        console.log("'waitFor()' timeout");
        phantom.exit(1);
      } else {
        typeof(onReady) === "string" ? eval(onReady) : onReady();
        clearInterval(interval);
      }
    }

  }, 100);

}

if (system.args.length !== 2) {
  console.log('Usage: run-qunit.js <URL>');
  phantom.exit(1);
}

var page = require('webpage').create();

// Route "console.log()" calls from within the Page context to the main Phantom context (i.e. current "this")
page.onConsoleMessage = function(msg) {
  console.log(msg);
};

// I admit, this is some pretty hairy, abstraction-free JavaScript. --JAB (10/13/13)
page.evaluateFailure = function() {

  var collectErrorStrs = function() {

    var origFilter = NodeList.prototype.filter;
    var origMap    = NodeList.prototype.map;

    NodeList.prototype.filter = Array.prototype.filter;
    NodeList.prototype.map    = Array.prototype.map;

    NodeList.prototype.zipWithIndex = function() {

      var arr = [];
      var x   = 0;

      this.map(function(that) {
        arr.push([that, x]);
        x++;
      });

      return arr;

    };

    var failedGroups = document.querySelectorAll('.fail').filter(function(elem) { return elem.getAttribute('id') !== null; });

    var errorStrs = failedGroups.map(function(group) {

      var assertList = group.querySelector('.qunit-assert-list');
      var zipped     = assertList.childNodes.zipWithIndex();
      var testPairs  = zipped.filter(function(z) { return z[0].getAttribute('class') === 'fail'; });
      var descNodes  = group.childNodes[0].childNodes;
      var group      = descNodes[0].innerHTML;
      var test       = descNodes[2].innerHTML;

      var failStrs = testPairs.map(function(testPair) {

        var extract = function(className) {
          var parts = testPair[0].querySelector('.' + className).childNodes;
          var label = parts[0].innerHTML;
          var value = parts[1].childNodes[0].innerHTML;
          return label + value;
        };

        var results     = ['test-expected', 'test-actual', 'test-source'].map(extract);
        var errorHeader = 'Test #' + (testPair[1] + 1) + ': ' + results[0];
        return results.slice(1).reduce(function(acc, result) { return acc + ', ' + result; }, errorHeader);

      });

      var failHeader = 'FAILED: ' + group + ' - ' + test;
      return failStrs.reduce(function(acc, str) {
        return acc + '\n-' + str;
      }, failHeader);


    });

    NodeList.prototype.filter = origFilter;
    NodeList.prototype.map    = origMap;

    return errorStrs.slice(1).reduce(function(acc, str) { return acc + '\n' + str; }, errorStrs[0]);

  };

  var el = document.getElementById('qunit-testresult');
  console.log(collectErrorStrs() + '\n' + el.innerText);

  try {
    return el.getElementsByClassName('failed')[0].innerHTML;
  } catch(e) {
    return 10000;
  }

};

page.open(system.args[1], function(status) {

  if (status !== "success") {
    console.log("Unable to access network");
    phantom.exit(1);
  } else {

    var successFunc = function() {
      return page.evaluate(function() {
        var elem = document.getElementById('qunit-testresult');
        return elem && elem.innerText.match('completed');
      });
    };

    var failFunc = function() {
      var failedNum  = page.evaluate(page.evaluateFailure);
      var statusCode = (parseInt(failedNum, 10) > 0) ? 1 : 0;
      phantom.exit(statusCode);
    }

    waitFor(successFunc, failFunc);

  }

});

