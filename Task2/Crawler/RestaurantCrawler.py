from bs4 import BeautifulSoup
import requests
import re
import pandas as pd
import json
from datetime import date, timedelta, datetime
import dateparser
from unidecode import unidecode
import sys

# RETRIEVE THE BeautifulSoup OBJECT OF A URL
def get_soup(url):
    headers = {
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)'
                      ' AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.108 Safari/537.36'}
    r = s.get(url, headers=headers)

    if r.status_code != 200:
        print('status code:', r.status_code)
    else:
        soup = BeautifulSoup(r.text, 'html.parser')
        return soup


# RETRIEVE THE NEXT PAGE
def find_page(current_url, response):
    if not response:
        print('no response:', current_url)
        return None
    next_page = response.find('span', class_='pageNum current')
    next_page = next_page.find_next('a', class_='pageNum taLnk')
    if next_page is None:
        return None
    next_url = next_page.get('href')
    next_url = "https://www.tripadvisor.it" + next_url
    city_urls.append(next_url)
    return next_url


# RETRIEVE THE RESTAURANT
def find_restaurants(current_url, response):
    if not response:
        print('no response:', current_url)
        return
    for a in response.find_all('a', class_='restaurants-list-ListCell__restaurantName--2aSdo'):
        start_urls.append("https://www.tripadvisor.it" + a['href'])


# PARSE THE RETRIEVED RESTAURANT
def parse(url, response):
    if not response:
        print('no response:', url)
        return

    # json object
    restaurant = {}

    # find returns None if can't find anything
    # find_all returns [] if can't find anything

    # get name

    restaurant_name = response.find('h1', class_='ui_header h1')
    if restaurant_name is None:
        return restaurant_name
    else:
        restaurant_name = restaurant_name.get_text()
        restaurant["name"] = restaurant_name

    # ----------------------------------------------------------------------------------------------------

    # get address

    # json object
    total_address = {}
    address = response.find('span', class_='street-address')
    if address is not None:
        address = address.get_text()
        address = address.replace(",", "")
        address = address.strip()
        total_address["street"] = address

    locality = response.find('span', class_='locality')
    if locality is not None:
        locality = locality.get_text()
        locality = locality.replace(",", "")
        locality = locality.strip()
        total_address["locality"] = locality

    country_name = response.find('span', class_='country-name')
    if country_name is not None:
        country_name = country_name.get_text()
        country_name = country_name.replace(",", "")
        country_name = country_name.strip()
        total_address["country"] = country_name
    restaurant["address"] = total_address

    # ----------------------------------------------------------------------------------------------------

    restaurant["approved"] = True

    # ----------------------------------------------------------------------------------------------------

    # get phone number

    phone_number = response.find('span', class_='detail is-hidden-mobile')
    if phone_number is not None:
        restaurant["phoneNumber"] = phone_number.get_text()

    # ----------------------------------------------------------------------------------------------------

    # get opening hours

    r = requests.get(url)
    times = re.findall('."days":\s*"[A-Za-z]+\s*.\s*[A-Za-z]+","times":.\s*"[0-9]+.[0-9]+\s*.\s*[0-9]+.[0-9]+'
                       '".(?:"[0-9]+.[0-9]+\s*.\s*[0-9]+.[0-9]+".)?.', r.text)
    # vector of json objects
    opening_hours = []
    if times:
        # to have unique objects
        times = set(times)
        for time in times:
            opening_hour = json.loads(time)
            opening_hours.append(opening_hour)
        restaurant["openingHours"] = opening_hours

    # ----------------------------------------------------------------------------------------------------

    info = response.find_all('div',
                             class_='restaurants-detail-overview-cards-DetailsSectionOverviewCard__categoryTitle--2RJP_')
    # json object
    prices = {}

    # vectors of strings
    types_list = []
    moments_list = []
    options_list = []
    functions_list = []

    for elem in info:
        elem_text = elem.get_text()
        next_elem = elem.find_next('div', class_='restaurants-detail-overview-cards-DetailsSectionOverviewCard__tagText--1OH6h')
        if "FASCIA PREZZO" in elem_text:
            price_range = next_elem
            if price_range is not None:
                price_range = price_range.get_text()
                price_range = price_range.replace("€", "")
                price_range = price_range.split("-")
                prices["minPrice"] = int(price_range[0])
                prices["maxPrice"] = int(price_range[1])
                restaurant["priceRange"] = prices
        if "CUCINE" in elem_text:
            type_of_cooking = next_elem
            if type_of_cooking is not None:
                type_of_cooking = type_of_cooking.get_text()
                for cooking in type_of_cooking.split(","):
                    cooking = cooking.strip()
                    types_list.append(cooking)
                restaurant["typesOfCooking"] = types_list
        if "Pasti" in elem_text:
            moments = next_elem
            if moments is not None:
                moments = moments.get_text()
                for moment in moments.split(","):
                    moment = moment.strip()
                    moments_list.append(moment)
                restaurant["mealtimes"] = moments_list
        if "Diete speciali" in elem_text:
            options = next_elem
            if options is not None:
                options = options.get_text()
                for option in options.split(","):
                    option = option.strip()
                    options_list.append(option)
                restaurant["options"] = options_list
        if "FUNZIONALITÀ" in elem_text:
            functions = next_elem
            if functions is not None:
                functions = functions.get_text()
                for function in functions.split(","):
                    function = function.strip()
                    functions_list.append(function)
                restaurant["features"] = functions_list

    # ----------------------------------------------------------------------------------------------------

    # get number of reviews

    reviews = response.find('div', class_='pagination-details')
    if reviews is not None:
        reviews = reviews.find_all('b')[2]
        num_reviews = int(reviews.get_text().replace(".", ""))
        restaurant["numberOfReviews"] = num_reviews
    else:
        reviews = response.find('span', class_='reviewCount')
        if reviews is not None:
            reviews = reviews.get_text()
            num_reviews = reviews.split(" ")
            num_reviews = num_reviews[0]
            # conversion to integer value
            num_reviews = int(num_reviews.replace(".", ""))
            restaurant["numberOfReviews"] = num_reviews

    # ----------------------------------------------------------------------------------------------------

   # vector of json objects
    ratings_list = []

    aggregated_ratings = re.findall('"aggregateRating":{".type":"AggregateRating"."ratingValue":"\d.\d"."reviewCount":"\d\d\d"}', r.text)
    if aggregated_ratings:
        aggregated_ratings = aggregated_ratings[0]
    if "ratingValue" in aggregated_ratings:
        aggregated_ratings = aggregated_ratings.replace('"aggregateRating":', "")
        aggregated_ratings = json.loads(aggregated_ratings)
        rating = {}
        rating["name"] = "Globale"
        value = aggregated_ratings.pop("ratingValue")
        rating["rating"] = float(value)
        ratings_list.append(rating)
    # ----------------------------------------------------------------------------------------------------

    ratings = re.findall('"ratingQuestions":.(?:."name":"Cucina".\s*"rating":\s*\d*.\s*"icon":"\w*".)?.?'
                         '(?:."name":\s*"Servizio".\s*"rating":\s*\d*.\s*"icon":"\w*".)?.?'
                         '(?:."name":\s*"Qualit.......prezzo".\s*"rating":\s*\d*.\s*"icon":\s*"\w*.\w*".)?.?'
                         '(?:."name":\s*"Atmosfera".\s*"rating":\s*\d*.\s*"icon":\s*"\w*"..)?', r.text)

    if ratings:
        ratings = ratings[0]
        if "name" in ratings:
            ratings = ratings.replace('"ratingQuestions":', "")
            ratings = ratings.replace("[", "")
            ratings = ratings.replace("]", "")
            ratings = ratings.replace("}", "}}")
            ratings = ratings.split("},")
            ratings[-1] = ratings[-1].replace("}}", "}")
            for rating in ratings:
                rating = json.loads(rating)
                rating["rating"] = rating["rating"] / 10
                rating.pop("icon", None)
                ratings_list.append(rating)
            restaurant["ratings"] = ratings_list

    # ----------------------------------------------------------------------------------------------------

    # get reviews

    url = url.replace('-Reviews-', '-Reviews-or{}-')
    print('template:', url)

    # vector of json objects
    reviews_list = []
    # load pages with reviews
    for offset in range(0, num_reviews, 10):
        url_ = url.format(offset)
        find_reviews(url_, get_soup(url_), r.text, reviews_list, num_reviews)

    restaurant["reviews"] = reviews_list

    return restaurant


# RETRIEVE REVIEWS OF A RESTAURANT
def find_reviews(url, response, source_code, review_list, num_reviews):
    if not response:
        print('no response:', url)
        return

    divs = response.find_all('div', class_='ui_column is-9')

    for div in divs:
        if len(review_list) >= num_reviews:
            return

        # for div, match in zip(divs, regex.finditer(source_code)):
        review = {}

        href = div.find('a', class_='title')

        if href:
            href = href.get('href')
            res = get_soup("https://www.tripadvisor.it" + href)

            if res is None:
                continue

            # ----------------------------------------------------------------------------------------------------

            # get date

            review_date_span = res.find('span', class_='ratingDate relativeDate')
            # le domande hanno lo stesso div ma non hanno lo span dentro
            if not review_date_span:
                return
            review_date = review_date_span.get_text()
            if review_date is not None:
                if "Recensito il " in review_date:
                    review_date = review_date.split("Recensito il ")[1]
                    r_date = dateparser.parse(review_date, languages=['it'])
                elif "Recensito " in review_date:
                    review_date = review_date.split("Recensito ")[1]
                    r_date = dateparser.parse(review_date, languages=['it'])
                r_date = r_date.date()
                review["date"] = str(r_date)

            # ----------------------------------------------------------------------------------------------------

            ratings = []

            rating = {}

            # get global rating

            review_span = res.find('span', class_=re.compile("ui_bubble_rating bubble_.0"))
            if review_span is not None:
                review_span = review_span.get('class')[1]
                score = int(int(review_span.split("_")[1]) / 10)
                if score is not None:
                    rating["name"] = "Globale"
                    rating["rating"] = float(score)
                    ratings.append(rating)

            # ----------------------------------------------------------------------------------------------------

            # get single ratings

            ratings_list = res.find('div', class_='rating-list')
            ratings_list = ratings_list.find('li')
            if ratings_list:
                ratings_list = ratings_list.find_all('ul')
                for elem in ratings_list:
                    li = elem.find_all('li')
                    for l in li:
                        rating = {}
                        name = l.find('div', class_='recommend-description').get_text()
                        if "Cibo" in name:
                            name = "Cucina"
                        rating["name"] = name
                        value = str(l.find('div'))
                        value = value.replace("ui_bubble_rating bubble_", "")
                        value = value.split("><")[0]
                        value = value.split("class=")[1]
                        value = value.replace('"', '')
                        value = int(value) / 10
                        rating["rating"] = value
                        ratings.append(rating)

            review["ratings"] = ratings

            # ----------------------------------------------------------------------------------------------------

            review_block = res.find('div',
                                    class_='review hsx_review ui_columns is-multiline is-mobile inlineReviewUpdate provider0')

            # get title

            review_title = review_block.find('span', class_='noQuotes')
            if review_title:
                review_title = review_title.get_text()
                if review_title is not None:
                    review["title"] = review_title

            # ----------------------------------------------------------------------------------------------------

            # get text

            review_text_div = review_block.find('p', class_='partial_entry')
            if review_text_div is not None:
                review_text = review_text_div.get_text()
                review["text"] = review_text

            # ----------------------------------------------------------------------------------------------------

            # get username

            username = review_block.find('span', class_='expand_inline scrname')
            if username is not None:
                username = username.get_text()
                review["username"] = username

            # ----------------------------------------------------------------------------------------------------

            # get reply of restaurant owner

            owner_reply = review_block.find('div', class_='mgrRspnInline')
            if owner_reply:
                reply = owner_reply.find_next('p', class_='partial_entry')
                if reply:
                    reply = reply.get_text()
                    review["ownerReply"] = reply
                owner_username = owner_reply.find('div', class_='header')
                if owner_username:
                    owner_username = owner_username.get_text()
                    owner_username = owner_username.split(",")[0]
                    review["ownerUsername"] = owner_username

            review_list.append(review)

        # ----------------------------------------------------------------------------------------------------

    return review_list


# MAIN

start_urls = []
"""
city = "pisa"
city_urls = ['https://www.tripadvisor.it/Restaurants-g187899-Pisa_Province_of_Pisa_Tuscany.html']
"""

"""
city = "firenze"
city_urls = ['https://www.tripadvisor.it/Restaurants-g2043770-Province_of_Florence_Tuscany.html']
"""

city = "siena"
city_urls = ['https://www.tripadvisor.it/Restaurants-g2043779-Province_of_Siena_Tuscany.html']

"""
city = "livorno"
city_urls = ['https://www.tripadvisor.it/Restaurants-g2043772-Province_of_Livorno_Tuscany.html']
"""

"""
city = "arezzo"
city_urls = ['https://www.tripadvisor.it/Restaurants-g2043769-Province_of_Arezzo_Tuscany.html']
"""

s = requests.Session()

city_url = city_urls[0]

# get just the first 10 pages (about 300 restaurants for each city)
while (len(city_urls) < 10) and (city_url is not None):
    city_url = find_page(city_url, get_soup(city_url))

for city_url in city_urls:
    find_restaurants(city_url, get_soup(city_url))

# print(start_urls)

for restaurant_url in start_urls:
    start = datetime.now()

    # parse information for each url
    restaurant = parse(restaurant_url, get_soup(restaurant_url))

    if restaurant is None:
        continue

    punctuations = '''!()-[]{};:'"\,<>./?@#$%^&*_~'''

    try:
        json_file_name = restaurant["name"].lower().replace(" ", "")

        formatted_name = ""
        for char in json_file_name:
            if char not in punctuations:
                formatted_name = formatted_name + char

        json_file_name = "./../data/" + city + "-" + formatted_name + ".json"
        json_file = open(json_file_name, 'w', encoding='utf-8')
        json.dump(restaurant, json_file, ensure_ascii=False, indent=4)
    except:
        print("Error in writing " + json_file_name + " " + str(sys.exc_info()[0]))
        raise

    end = datetime.now()
    print(end - start)
