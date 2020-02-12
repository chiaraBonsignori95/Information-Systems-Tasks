import json
from os import listdir
from os.path import isfile, join
import sys

data_dir = "./../data/"
json_files = [f for f in listdir(data_dir) if isfile(join(data_dir, f))]

parsed_data_dir = "./../parsed_data/"

categories = []
options = []
features = []
mealtimes = []

for json_file in json_files:

    file_city = json_file.split("-")[0]
    file_city = file_city.capitalize()
    try:
        restaurant = json.load(open(data_dir + json_file, encoding="utf8"))

        # rename "typesOfCooking" in "categories"
        if restaurant.get("typesOfCooking") is not None:
            typesOfCooking = restaurant.pop("typesOfCooking")
            if typesOfCooking is not None:
                restaurant["categories"] = typesOfCooking

        restaurantCategories = restaurant.get("categories")
        if restaurantCategories is not None:
            for category in restaurantCategories:
                categories.append(category)

        restaurantOptions = restaurant.get("options")
        if restaurantOptions is not None:
            for option in restaurantOptions:
                options.append(option)

        restaurantFeatures = restaurant.get("features")
        if restaurantFeatures is not None:
            for feature in restaurantFeatures:
                features.append(feature)

        restaurantMealtimes = restaurant.get("mealtimes")
        if restaurantMealtimes is not None:
            for mealtime in restaurantMealtimes:
                mealtimes.append(mealtime)

        # set rating as float
        if restaurant.get("ratings") is not None:
            ratings = restaurant.pop("ratings")
            global_rating = None
            total = 0
            count = 0
            for rating in ratings:
                total = total + rating.get("rating")
                count = count + 1
                if rating.get("name") == "Globale":
                    global_rating = rating.get("rating")
                    break
            if global_rating is not None:
                restaurant["rating"] = global_rating
            else:
                global_rating = float(total/count)
                if (global_rating > 0.5) and (global_rating < 1):
                    global_rating = 0.5
                if (global_rating > 1) and (global_rating < 1.5):
                    global_rating = 1
                if (global_rating > 1.5) and (global_rating < 2):
                    global_rating = 1.5
                if (global_rating > 2) and (global_rating < 2.5):
                    global_rating = 2
                if (global_rating > 2.5) and (global_rating < 3):
                    global_rating = 2.5
                if (global_rating > 3) and (global_rating < 3.5):
                    global_rating = 3
                if (global_rating > 3.5) and (global_rating < 4):
                    global_rating = 3.5
                if (global_rating > 4) and (global_rating < 4.5):
                    global_rating = 4
                if (global_rating > 4.5) and (global_rating < 5):
                    global_rating = 5

                restaurant["rating"] = global_rating

        # replace "locality" with "city" and "postcode"
        locality = restaurant.get("address").pop("locality")
        locality = locality.split(" ", 1)
        if len(locality) == 2:
            cap = locality[0]
            city = locality[1]
            restaurant["address"]["city"] = city
            restaurant["address"]["postcode"] = cap
        else:
            restaurant["address"]["city"] = locality[0]

        address = restaurant.get("address")
        if address.get("city") not in ["Pisa", "Firenze", "Siena", "Livorno", "Arezzo"]:
            address["city"] = file_city
            #print(json_file)

        parsed_reviews = []
        if restaurant.get("reviews") is not None:
            reviews = restaurant.pop("reviews")
            for review in reviews:
                # set rating as float
                review_ratings = review.pop("ratings")
                review_global_rating = None
                for review_rating in review_ratings:
                    if review_rating.get("name") == "Globale":
                        review_global_rating = review_rating.get("rating")
                        break
                if review_global_rating is not None:
                    review["rating"] = review_global_rating
                else:
                    print("Error in parsing review rating in " + restaurant)

                # set reply as embedded document
                if review.get("ownerReply") is not None:
                    owner_reply = review.pop("ownerReply")
                    reply = {}
                    reply["text"] = owner_reply
                    if review.get("ownerUsername") is not None:
                        reply["owner"] = review.pop("ownerUsername")
                    review["reply"] = reply

                if len(review.get("text")) == 0:
                    review.pop("text")

                if len(review.get("title")) == 0:
                    review.pop("title")

                parsed_reviews.append(review)

            restaurant["reviews"] = parsed_reviews

        file = open(parsed_data_dir + json_file, 'w', encoding='utf-8')
        json.dump(restaurant, file, ensure_ascii=False, indent=4, sort_keys=True)

    except Exception:
        print("Error in writing " + json_file)
        print(str(sys.exc_info()))

categories = list(dict.fromkeys(categories))
categories.sort()
with open('categories.txt', 'w', encoding='utf-8') as f:
    for item in categories:
        f.write("%s\n" % item)

options = list(dict.fromkeys(options))
options.sort()
with open('options.txt', 'w', encoding='utf-8') as f:
    for item in options:
        f.write("%s\n" % item)

features = list(dict.fromkeys(features))
features.sort()
with open('features.txt', 'w', encoding='utf-8') as f:
    for item in features:
        f.write("%s\n" % item)

mealtimes = list(dict.fromkeys(mealtimes))
mealtimes.sort()
with open('mealtimes.txt', 'w', encoding='utf-8') as f:
    for item in mealtimes:
        f.write("%s\n" % item)