(ns cloft.recipe
  (:import [org.bukkit Bukkit Material])
  (:import [org.bukkit.inventory ItemStack ShapelessRecipe])
  (:import [org.bukkit.util Vector]))

(def string-web
  (let [x (ShapelessRecipe. (ItemStack. Material/WEB 3))]
    (.addIngredient x 3 Material/STRING)
    x))

(def gravel-flint
  (let [x (ShapelessRecipe. (ItemStack. Material/FLINT 1))]
    (.addIngredient x 3 Material/GRAVEL)
    x))

(def flint-gravel
  (let [x (ShapelessRecipe. (ItemStack. Material/GRAVEL 3))]
    (.addIngredient x 1 Material/FLINT)
    x))

(def seed-coal
  (let [x (ShapelessRecipe. (ItemStack. Material/COAL 1))]
    (.addIngredient x 4 Material/SEEDS)
    x))

(defn on-enable []
  (Bukkit/addRecipe string-web)
  (Bukkit/addRecipe gravel-flint)
  (Bukkit/addRecipe flint-gravel)
  (Bukkit/addRecipe seed-coal))
