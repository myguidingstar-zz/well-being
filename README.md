# Well-being

An over-happy library to process raw data set of wells and other types
of water points and report their functioning state (Pun
intended).

This is an experiment with transducers.

## Usage

 Just give it an URL, `well-being` will go find the data by itself.
```clj
(require [well-being.core :refer [calculate]])

(calculate "http;//some-url/water_points.json")
```

For internal functions' usage, see [unit tests](test/well_being/core_test.clj)

## Design architectue
See [document](Architecture.md) for details.

## License

Copyright © 2015 Hoang Minh Thang

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
