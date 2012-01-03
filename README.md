# About

HoneyQL is a sweet little SQL for Factual's public API. It comes with a command line interface that lets you run SQL-like query statements against Factual's data.

Here's an example of using HoneyQL to find restaurants near Factual that deliver dinner, sorted by distance:

<pre>
> select name, category, price, rating, _distance \
  from restaurants-us near('1801 avenue of the stars, los angeles, ca') \
  where meal_deliver = true and meal_dinner = true \
  order by _distance limit 12

+---------------------------+-----------------------------------------+-----+------+---------+
|name                       |category                                 |price|rating|$distance|
+---------------------------+-----------------------------------------+-----+------+---------+
|Johnnie's New York Pizzeria|Food & Beverage > Restaurants > Pizza    |2    |2.5   |253.5255 |
+---------------------------+-----------------------------------------+-----+------+---------+
|Take A Bao                 |Food & Beverage > Restaurants            |2    |4.0   |343.15497|
+---------------------------+-----------------------------------------+-----+------+---------+
|Stand                      |Food & Beverage > Restaurants > Fast Food|1    |3.5   |375.0205 |
+---------------------------+-----------------------------------------+-----+------+---------+
|Habanero Grill             |Food & Beverage > Restaurants            |2    |4.0   |468.32678|
+---------------------------+-----------------------------------------+-----+------+---------+
|Cuvee                      |Food & Beverage > Restaurants            |2    |2.0   |468.32678|
+---------------------------+-----------------------------------------+-----+------+---------+
|Joss Cuisine               |Food & Beverage > Restaurants            |4    |4.5   |696.335  |
+---------------------------+-----------------------------------------+-----+------+---------+
|Roni's Diner               |Food & Beverage > Restaurants            |2    |4.0   |731.27856|
+---------------------------+-----------------------------------------+-----+------+---------+
|Subway                     |Food & Beverage > Restaurants > Fast Food|null |2.0   |752.7942 |
+---------------------------+-----------------------------------------+-----+------+---------+
|Ingrid's                   |Food & Beverage > Restaurants            |2    |4.5   |887.0573 |
+---------------------------+-----------------------------------------+-----+------+---------+
|Cool Basil                 |Food & Beverage > Restaurants            |2    |4.0   |1123.901 |
+---------------------------+-----------------------------------------+-----+------+---------+
|IL Forno Caldo             |Food & Beverage > Restaurants            |2    |4.0   |1291.0625|
+---------------------------+-----------------------------------------+-----+------+---------+
|Cafe Modilani              |Food & Beverage > Restaurants            |2    |3.5   |1312.0791|
+---------------------------+-----------------------------------------+-----+------+---------+
</pre>

# Installation

Make sure you have:

* Java installed on your machine
* A valid Factual API key and secret

[Download Honey 1.0.1](http://github.com/dirtyvagabond/honey-ql/blob/master/bin/honey_1_0_1.jar?raw=true) to your machine...

... and run it like this:

    java -jar honey_1_0_1.jar

# Supported SQL Subset

* SELECT
* FROM
* WHERE
* ORDER BY
* LIMIT
* OFFSET

# HoneyQL Specific Syntax

* SEARCH (Full Text Search)
* NEAR (Geo proximity filter, integrated with Data Science Toolkit)

# SELECT

HoneyQL query statements begin with SELECT. Simple examples:

    > select * from places

    > select name, tel, website from restaurants-us

# WHERE (row filters)

HoneyQL supports a WHERE clause for row filtering.

## Supported filter operations

* =
* !=
* >
* <
* <=
* >=
* like
* in
* not in
* is null
* is not null

## LIKE

You can use the LIKE syntax to do "begins with" matches. For example:

    > select name from places where name like 'starbuck%'

You cannot do "ends with" matches with HoneyQL.

If you surround your term with %, or use no %s, it's treated like a column-specific Full Text Search.

## IN and NOT IN

You can use IN and NOT IN to row filter with lists. For example:

See Los Angeles restaurants founded recently:

    > select name, founded from restaurants-us where locality = 'Los Angeles' and founded in (2010, 2011, 2012)

See Los Angeles restaurants that won't make me dress too nice:

    > select name, attire from restaurants-us where locality = 'Los Angeles' and attire not in ('formal', 'smart casual', 'business casual')

## More Examples of row filtering

Find Los Angeles restaurants having an official "attire required" policy:

    > select name, attire_required from restaurants-us where locality = 'los angeles' and attire_required is not null

View Los Angeles restaurant owners in descending order of their price rating:

    > select name, owner, price from restaurants-us where locality = 'los angeles' and owner is not null order by price desc

View business in Chiang Mai, Thailand that are operational and have a telephone number:

    > select name, tel from global where country = 'TH' and region = 'Chiang Mai' and status = 1 and tel is not null

## Nested logic

You can nest your row filter logic just like you can with SQL, including the use of (), AND, and OR. For example:

Find Los Angeles restaurants that have wifi or smoking:

    > select name, wifi, smoking from restaurants-us where locality = 'Los Angeles' and (wifi = true or smoking = true) 

# Full Text Search (SEARCH)

HoneyQL supports a custom search() syntax for full text searches across whole rows. For example:

    > select name from places search('cafe')

You can use SEARCH along with a WHERE clause. For example:

    > select name from places search('cafe') where category like 'Food%'

If you want to do Full Text Search for only a specific column, you use <tt>like</tt> with no wildcards. For example:

    > select name from places where name like 'starbucks'

# Geo Proximity Filter (NEAR)

The NEAR syntax lets you describe a place's address with free text and will attempt to locate that place then find places near to it.

For example, find places near Factual:

    > select name from places near('1801 avenue of the stars, los angeles, ca')

This feature queries the Data Science Toolkit to do the coordinate lookup based on your address term. The query will fail if Data Science Toolkit cannot find a match. This often happens if your address text is unclear or incomplete.

You can use NEAR along with a WHERE clause. For example:

    > select name from restaurants-us near('1801 avenue of the stars, los angeles, ca') where meal_deliver = true and meal_dinner = true

Same as above, plus sort by distance:

    > select name, _distance from restaurants-us near('1801 avenue of the stars, los angeles, ca') where meal_deliver = true and meal_dinner = true order by _distance
