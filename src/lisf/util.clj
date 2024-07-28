(ns lisf.util)

(defn map-cond
  "Apply the function to the given value only if the condition is truth"
  [c fn value]
  (if c (fn value) value))

(defn |> [value & fns]
  (reduce #(%2 %1) value fns))

