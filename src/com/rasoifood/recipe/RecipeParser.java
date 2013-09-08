/**
 * 
 */
package com.rasoifood.recipe;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.rasoifood.object.AgriProduct;
import com.rasoifood.object.Ingredient;
import com.rasoifood.object.Recipe;

/**
 * @author Admin
 * 
 */
public class RecipeParser {

	public static Map<Ingredient, List<Recipe>> ingredientRecipeMap = new HashMap<Ingredient, List<Recipe>>();
	public static Map<String, Ingredient> allIngredients = new TreeMap<String, Ingredient>();
	public static Map<String, Recipe> allRecipes = new HashMap<String, Recipe>();
	public static Map<String, String> translations = new HashMap<String, String>();
	public static Map<String, Map<String, Integer>> priceMap = new HashMap<String, Map<String, Integer>>();
	private static Random rand = new Random();
	
	public static void parseTranslationFile() {
		File buildPath = new File(System.getProperty("java.class.path"));
		File translationFile = new File(buildPath.getParent() + File.separator
				+ "config" + File.separator + "translation.txt");
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(translationFile));
			StringTokenizer tok = null, tok2 = null;
			String line = br.readLine();

			while (line != null) {
				tok = new StringTokenizer(line, "::");
				String str = tok.nextToken().trim().toLowerCase();
				String val = tok.nextToken().trim().toLowerCase();
				if (str.indexOf(",") == -1) {
					translations.put(str, val);
				} else {
					tok2 = new StringTokenizer(str, ",");
					while (tok2 != null && tok2.hasMoreTokens()) {
						translations.put(tok2.nextToken().trim(), val);
					}
				}
				line = br.readLine();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (IOException e) {
			}
		}
	}

	public static void parseRecipeFile() {
		File buildPath = new File(System.getProperty("java.class.path"));
		File recipeFile = new File(buildPath.getParent() + File.separator
				+ "config" + File.separator + "recipe.txt");
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(recipeFile));
			StringTokenizer tok = null, tok2 = null;
			String line = br.readLine();

			while (line != null) {
				tok = new StringTokenizer(line, "::");
				Recipe r = new Recipe();
				r.setId(Integer.parseInt(tok.nextToken().trim()));
				r.setName(tok.nextToken().trim());
				r.setDish(tok.nextToken().trim());
				r.setCuisine(tok.nextToken().trim());
				r.setCookingTime(Integer.parseInt(tok.nextToken().trim()));
				r.setImageName(buildPath.getParent() + File.separator
						+ "images" + File.separator + r.getName().toLowerCase() + ".jpg");

				allRecipes.put(r.getName(), r);

				List<Ingredient> ingredients = new ArrayList<Ingredient>();
				r.setIngredients(ingredients);
				String ing = tok.nextToken().trim();
				tok2 = new StringTokenizer(ing, ";");
				while (tok2 != null && tok2.hasMoreTokens()) {
					Ingredient i = new Ingredient();
					i.setName(tok2.nextToken().trim());
					ingredients.add(i);
					List<Recipe> list = ingredientRecipeMap.get(i);
					if (list == null) {
						list = new ArrayList<Recipe>();
						ingredientRecipeMap.put(i, list);
					}
					list.add(r);
					if (!allIngredients.containsKey(i.getName())) {
						allIngredients.put(i.getName(), i);
					}
				}

				r.setSteps(tok.nextToken().trim());
				line = br.readLine();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (IOException e) {
			}
		}

	}

	public static void parseMandiPrices(String url) {
		if (url != null && !url.isEmpty()) {
			try {
				URL xmlURL = new URL(url);
				URLConnection connection = xmlURL.openConnection();

				DocumentBuilder builder = DocumentBuilderFactory.newInstance()
						.newDocumentBuilder();
				Document doc = builder.parse(connection.getInputStream());
				NodeList nodes = doc.getElementsByTagName("Table");

				for (int i = 0; i < nodes.getLength(); i++) {
					NodeList children = nodes.item(i).getChildNodes();
					AgriProduct product = new AgriProduct();
					for (int j = 0; j < children.getLength(); j++) {
						populateAgriProduct(children.item(j), product);
					}
					Map<String, Integer> map = priceMap.get(product
							.getCommodity());
					if (map == null) {
						map = new HashMap<String, Integer>();
						priceMap.put(product.getCommodity().toLowerCase(), map);
					}
					if (!map.containsKey(product.getState() + "::"
							+ product.getDistrict() + "::"
							+ product.getMarket())) {
						map.put(product.getState() + "::"
								+ product.getDistrict() + "::"
								+ product.getMarket(), product.getPrice());
					}
				}
				// System.out.println(priceMap.size());
				// System.out.println(priceMap.get("Potato").toString());
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			}
		}
	}

	private static void populateAgriProduct(Node node, AgriProduct product) {
		if (node.getNodeName().equalsIgnoreCase("State")) {
			product.setState(node.getTextContent());
		} else if (node.getNodeName().equalsIgnoreCase("District")) {
			product.setDistrict(node.getTextContent());
		} else if (node.getNodeName().equalsIgnoreCase("Market")) {
			product.setMarket(node.getTextContent());
		} else if (node.getNodeName().equalsIgnoreCase("Commodity")) {
			product.setCommodity(node.getTextContent());
		} else if (node.getNodeName().equalsIgnoreCase("Arrival_Date")) {
			product.setArrivalDate(node.getTextContent());
		} else if (node.getNodeName().equalsIgnoreCase("Modal_x0020_Price")) {
			product.setPrice(Integer.parseInt(node.getTextContent()));
		}
	}

	public static Set<String> getAllIngredientsList() {
		return allIngredients.keySet();
	}

	public static List<Recipe> getRecipesForIngredient(String name) {
		if (name != null) {
			return ingredientRecipeMap.get(allIngredients.get(name));
		}
		return null;
	}

	public static List<Recipe> getRecipesForAllIngredients(
			List<String> inclusions) {
		if (inclusions != null && inclusions.size() > 0) {
			Iterator<String> iter = inclusions.iterator();
			List<Recipe> recipes = ingredientRecipeMap.get(allIngredients
					.get(iter.next()));
			if (recipes != null && recipes.size() > 0) {
				while (iter != null && iter.hasNext()) {
					Ingredient ing = allIngredients.get(iter.next());
					Iterator<Recipe> recipeIter = recipes.iterator();
					while (recipeIter != null && recipeIter.hasNext()) {
						Recipe r = recipeIter.next();
						if (!r.getIngredients().contains(ing)) {
							recipeIter.remove();
						}
					}
				}
				return recipes;
			}
		}
		return null;
	}

	public static List<Recipe> getRecipesForAllIngredients(
			List<String> inclusions, List<String> exclusions) {
		List<Recipe> recipes = getRecipesForAllIngredients(inclusions);
		if (recipes != null && recipes.size() > 0) {
			if (exclusions != null && exclusions.size() > 0) {
				Iterator<String> iter = exclusions.iterator();
				while (iter != null && iter.hasNext()) {
					Ingredient ing = allIngredients.get(iter.next());
					Iterator<Recipe> recipeIter = recipes.iterator();
					while (recipeIter != null && recipeIter.hasNext()) {
						Recipe r = recipeIter.next();
						if (r.getIngredients().contains(ing)) {
							recipeIter.remove();
						}
					}
				}
			}
			return recipes;
		}
		return null;
	}

	public static List<String> getCuisinesForRecipes(List<Recipe> recipes) {
		if (recipes != null && recipes.size() > 0) {
			List<String> cuisines = new ArrayList<String>();
			Iterator<Recipe> recipeIter = recipes.iterator();
			while (recipeIter != null && recipeIter.hasNext()) {
				Recipe r = recipeIter.next();
				cuisines.add(r.getCuisine());
			}
			return cuisines;
		}
		return null;
	}

	public static List<String> getCuisinesForIngredient(String name) {
		return getCuisinesForRecipes(getRecipesForIngredient(name));
	}

	public static List<String> getCuisinesForAllIngredients(
			List<String> inclusions) {
		return getCuisinesForRecipes(getRecipesForAllIngredients(inclusions));
	}

	public static List<String> getCuisinesForAllIngredients(
			List<String> inclusions, List<String> exclusions) {
		return getCuisinesForRecipes(getRecipesForAllIngredients(inclusions,
				exclusions));
	}

	public static List<String> getDishesForRecipes(List<Recipe> recipes) {
		if (recipes != null && recipes.size() > 0) {
			List<String> dishes = new ArrayList<String>();
			Iterator<Recipe> recipeIter = recipes.iterator();
			while (recipeIter != null && recipeIter.hasNext()) {
				Recipe r = recipeIter.next();
				dishes.add(r.getDish());
			}
			return dishes;
		}
		return null;
	}

	public static List<String> getDishesForIngredient(String name) {
		return getDishesForRecipes(getRecipesForIngredient(name));
	}

	public static List<String> getDishesForAllIngredients(
			List<String> inclusions) {
		return getDishesForRecipes(getRecipesForAllIngredients(inclusions));
	}

	public static List<String> getDishesForAllIngredients(
			List<String> inclusions, List<String> exclusions) {
		return getDishesForRecipes(getRecipesForAllIngredients(inclusions,
				exclusions));
	}

	public static int getWholesalePriceForCommodity(String commodity) {
		if (commodity != null) {
			Map<String, Integer> map = priceMap.get(commodity.toLowerCase());
			if (map != null && !map.isEmpty()) {
				Object[] obj = map.values().toArray();
				if (obj[0] instanceof Integer) {
					return ((Integer) obj[0]/100);
				}
			}
		}
		return -1;
	}

	public static int getRetailPriceForCommodity(String commodity) {
		if (commodity != null) {
			int w_price = getWholesalePriceForCommodity(commodity);
			if (w_price != -1) {
				return w_price + (int)(rand.nextDouble()*10);
			}
		}
		return -1;
	}
	
	public static String getTranslation(String ingredient) {
		if (ingredient != null) {
			String translatedName = ingredient;
			if (translations.containsKey(ingredient.trim())) {
				translatedName = translations.get(ingredient);
			} else {
				Iterator<String> keys = translations.keySet().iterator();
				while (keys != null && keys.hasNext()) {
					String ing = keys.next();
					if (ing.indexOf(ingredient.trim()) != -1) {
						System.out.println("Synonyms: " + ing + " - "
								+ ingredient);
						translatedName = translations.get(ing);
					}
				}
			}

			Iterator<String> keys = priceMap.keySet().iterator();
			while (keys != null && keys.hasNext()) {
				String ing = keys.next();
				if (ing.indexOf(ingredient.trim()) != -1) {
					System.out.println("Synonyms: " + ing + " - " + ingredient);
					return ing;
				}
				if (ing.indexOf(translatedName.trim()) != -1) {
					System.out.println("Synonyms: " + ing + " - "
							+ translatedName);
					return ing;
				}
			}
		}
		return ingredient;
	}

	public static void main(String args[]) {
		RecipeParser.parseRecipeFile();
		RecipeParser.parseTranslationFile();
		RecipeParser
				.parseMandiPrices("http://data.gov.in/sites/default/files/Date-Wise-Prices-all-Commodity.xml");
		System.out.println(priceMap.keySet());

		Iterator<String> iter = allIngredients.keySet().iterator();
		int counter = 0;
		while (iter != null && iter.hasNext()) {
			String ing = iter.next();
			int price = getRetailPriceForCommodity(ing);
			if (price == -1) {
				price = getRetailPriceForCommodity(getTranslation(ing));
			}
			if (price != -1) {
				counter++;
			}
			System.out.println(ing + " : " + price);
		}
		System.out.println("Total products with valid prices: " + counter);
	}

}
