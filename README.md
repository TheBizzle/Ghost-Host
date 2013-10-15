# Ghost Host

This SBT plugin allows for the running of JavaScript tests headlessly within SBT.  To use it, a local installation of [PhantomJS](http://phantomjs.org/) is required.

## Keys

  * `ghConfig`
    * A **setting** of a [`GhostHostConfig`](src/main/scala/org/bizzle/plugin/ghosthost/Config.scala) that describes where the root of the project is and which JS file(s) are being used
  * `debugSettings`
    * A **setting** of a [`DebugSettings`](src/main/scala/org/bizzle/plugin/ghosthost/Debug.scala) that determines whether or not debugging mode is enabled, and what to do when debugging mode is enabled.  By default, debugging mode launches the QUnit test page through the `google-chrome` command.  If you do not have Chrome on your `$PATH` as `google-chrome`, you'll probably want to change this default action
    * `org.bizzle.plugin.ghost.JustPause` is a nifty alternative action if you don't want to load the page in the browser
  * `moduleSpecs`
    * A **setting** of [`ModuleSpecs`](src/main/scala/org/bizzle/plugin/ghosthost/ModuleSpec.scala)s that describe what path(s) to look for resources at in a given dependency `.jar`.
  * `testJS`
    * A **task** that runs the JavaScript tests (i.e. the whole point of this plugin)

## Building

Simply run the `package` SBT command to build a new version of the plugin `.jar`.  Then, set your SBT project's `plugins.sbt` to reference/fetch the `.jar`.

## Terms of Use

This plugin is released under the BSD-3 license.  Please see [LICENSE.txt](LICENSE.txt) for further details.

The `run-qunit.js` file is heavily based on a third-party file and released under the BSD-3 license by Ariya Hidayat under the following terms:

> run-qunit (from the PhantomJS examples, with the addition of `page.evaluateFailure` by Jason Bertsche)
> Copyright (c) 2011, Ariya Hidayat
> All rights reserved.
>
> Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
>
> * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
> * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
> * Neither the name of Ariya Hidayat nor the names of project contributors may be used to endorse or promote products derived from this software without specific prior written permission.
>
> THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
