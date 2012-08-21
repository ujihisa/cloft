(ns cloft.recipe
  (:require [cloft.material :as m])
  (:import [org.bukkit Bukkit])
  (:import [org.bukkit.inventory ItemStack ShapelessRecipe])
  (:import [org.bukkit.util Vector]))

(def string-web
  (let [x (ShapelessRecipe. (ItemStack. m/web 3))]
    (.addIngredient x 3 m/string)
    x))

(def gravel-flint
  (let [x (ShapelessRecipe. (ItemStack. m/flint 1))]
    (.addIngredient x 3 m/gravel)
    x))

(def flint-gravel
  (let [x (ShapelessRecipe. (ItemStack. m/gravel 3))]
    (.addIngredient x 1 m/flint)
    x))

(def seed-coal
  (let [x (ShapelessRecipe. (ItemStack. m/coal 1))]
    (.addIngredient x 4 m/seeds)
    x))

(defn on-enable []
  (Bukkit/addRecipe string-web)
  (Bukkit/addRecipe gravel-flint)
  (Bukkit/addRecipe flint-gravel)
  (Bukkit/addRecipe seed-coal))
