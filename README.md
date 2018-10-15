# imstransport-web-site

A interactive Web presentation for the auto-transport firm 'Nikola Josifović'.
The main feature/usage of the site is to calculate the price of the transportation
by marking the start and end points on the map. Only considered area is Serbia and
all routes that are outside the borders are not calculated. Site has only Serbian
version.

The site was hosted under www.imamesvuda.rs. Because of some technical difficulties
with the DNS provider, we had to use [redirect.center](https://github.com/udleinati/redirect.center)
to route the domain name to the Heroku repo.

The firm was terminated in September 2018. and the site is released to public
under GNU general public license v3.

Non working version (Google Map API keys are disabled/expired) can be viewed
[here](https://imamesvuda.herokuapp.com).

## Developing

Some development documentation, as well as a todo list can be found [here](/doc).

### Setup

When you first clone this repository, run:

```sh
lein setup
```

This will create files for local configuration, and prep your system
for the project.

### Environment

To begin developing, start with a REPL.

```sh
lein repl
```

Then load the development environment.

```clojure
user=> (dev)
:loaded
```

Run `go` to initiate and start the system.

```clojure
dev=> (go)
:started
```

By default this creates a web server at <http://localhost:3000>.

When you make changes to your source files, use `reset` to reload any
modified files and reset the server. Changes to CSS or ClojureScript
files will be hot-loaded into the browser.

```clojure
dev=> (reset)
:reloading (...)
:resumed
```

If you want to access a ClojureScript REPL, make sure that the site is loaded
in a browser and run:

```clojure
dev=> (cljs-repl)
Waiting for browser connection... Connected.
To quit, type: :cljs/quit
nil
cljs.user=>
```

### Testing

Testing is fastest through the REPL, as you avoid environment startup
time.

```clojure
dev=> (test)
...
```

But you can also run tests through Leiningen.

```sh
lein test
```

### Generators

This project has several generator functions to help you create files.

To create a new endpoint:

```clojure
dev=> (gen/endpoint "bar")
Creating file src/foo/endpoint/bar.clj
Creating file test/foo/endpoint/bar_test.clj
Creating directory resources/foo/endpoint/bar
nil
```

To create a new component:

```clojure
dev=> (gen/component "baz")
Creating file src/foo/component/baz.clj
Creating file test/foo/component/baz_test.clj
nil
```

To create a new boundary:

```clojure
dev=> (gen/boundary "quz" foo.component.baz.Baz)
Creating file src/foo/boundary/quz.clj
Creating file test/foo/boundary/quz_test.clj
nil
```

## Deploying

Production version is deployed on Heroku environment.
Before running setup install Heroku cli.
To deploy run:

``` sh
lein deploy
```

## Legal

Copyright © 2016-2018 Auto-transport firm 'Nikola Josifović'.

View [license](/LICENSE.txt).
