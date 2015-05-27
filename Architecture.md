# Architecture for well-being

## Major steps:

### 1. fetch the given url and convert to Clojure data structure

output of this step should be a vector of hash-maps, each of which
presents a water point the name of community or village it is in and
its functional state.

example for such a hash-map:

```clj
{"communities_villages" "Kpikpaluk" "water_functioning" "yes"}
{"communities_villages" "Jaata" "water_functioning" "no"}
```
(other possible key/values are omitted for clarity)

### 2. iterate (using reduce) through the above vector to aggregate:

  - The number of water points that are functional

  - The information of water points per community, including:

    + number of broken ones

    + number of water points of any functioning state (broken, functional or
    unknown)

sample output of this step:

```clj
{:number-functional 6,
:aggregated-numbers {"Kpikpaluk" [0 4], "Jaata" [1 3]}}
```

in this sample, Kpikpaluk has 0 broken water point and 4 water points
of any functioning state. Those numbers in Jaata are 1 and 4.

Total number of water points in functioning state is 6. However, total
number of all water points is `4 + 3 = 7`, which means `7 - 6 = 1` of
them is in unknown functioning state.

### 3. From the aggregated data, calculate the required output information

- The number of water points that are functional: unchanged from
  previous step

- The number of water points per community: the second number of the
    two aggregated numbers.

- The rank for each community/village by the percentage of broken water points
  is calculated like this:

    + for each community/village, calculate the percentage by apply
      `/` to the two numbers

    + sort the above list of communities/villages by the percentage

    + `map-indexed` to get the rank (the ordinals in the sorted list)
      for each of them.

### 4. format Clojure data structure from previous steps as JSON

`cheshire.core/generate-string` is used again to generate JSON.
