# Swiss-Re-1345-1
Coffee Corner shop

## Prerequisites:
Java 8 or upper, Maven, IntelliJ or other ide

2. Run the application
a, mvn clean install and run from project dir
b, from Intellij right click on Application class, Run java -jar target/SwissRe-1354-1.jar from project dir

3. Maunal: it is a console application with menu navigation

## I. Main Menu:
1. Create new order
2. List previous orders
3. Quit (Q)

## II. Create Order
Small coffee    (sc)    2,50  CHF

Medium coffee   (mc)    3,00  CHF

Large coffee    (lc)    3,50  CHF

Bacon roll      (br)    4,50  CHF

Orange Juice    (oj)    3,95  CHF

Extra Milk      (xem)   0,30  CHF

Foamed Milk     (xfm)   0,50  CHF

Roasted Coffee  (rcf)   0,50  CHF


Please choose an other product with the code(second column) or submit your order(x), cancel(c): 

## II/a non-coffee product: type the code and the quantity
Please choose another product with the code(second column) or submit your order(x), cancel(c): 
br
Please type the quantity: 
2

## II/b coffee product: type the code the type of extra and the quantity
You can ignore the further extra selection with 'n' 

Please choose another product with the code(second column) or submit your order(x), cancel(c): 
sc
You can choose an extra with codes to your coffee product: xem, xfm, rcf or say no(n)
xem
You can choose another coffee extra with valid code: xfm, rcf or say no(n)!
n
Please type the quantity: 
3

## II/c Please choose another product with the code(second column) or submit your order(x), cancel(c): 
You can continue or submit or cancel your order here.

## III. You can track your orders and 5th beverage discounts with the second option fom the main menu.
1. Create new order
2. List previous orders
3. Quit (Q)

Please choose with a number: 
2

Order: 0

Bacon roll      (br)    4,50 X 2  9,00 CHF
Small coffee    (sc)    2,50 X 3  7,50 CHF
Extra Milk      (xem)   0,30 X 3  0,90 CHF

Total:                           17,40 CHF 
beverage1snack1                  -0,30 CHF
beverage1snack1                  -0,30 CHF

Total with discount:             16,80 CHF 

Order: 1

Orange Juice    (oj)    3,95 X 3  11,85 CHF

Total:                            11,85 CHF 
beverage5th                       -3,95 CHF

Total with discount:               7,90 CHF 

That's it :)
