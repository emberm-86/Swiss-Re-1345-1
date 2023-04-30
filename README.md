# Charlene's Coffee Corner
Coffee Corner shop

## Prerequisites:
Java 8 or higher, Maven, IntelliJ or other ide

## Running the application
- cd PROJECT_DIR, mvn clean install and run java -jar target/Swiss-Re-1354-1.jar
- from Intellij right-click on the Application class and click on Run

## Manual: it's a console application with menu navigation.

### I. Main Menu:
``` bash
========================
Coffee Corner
========================
1. Create new order
2. List previous orders
3. Quit (Q)
========================
```
You can create orders with the first option and track them and 5th beverage discounts with the second option from the main menu

### II. Create Order
``` bash
========================
Small coffee    (sc)    2,50 CHF
Medium coffee   (mc)    3,00 CHF
Large coffee    (lc)    3,50 CHF
Bacon roll      (br)    4,50 CHF
Orange Juice    (oj)    3,95 CHF
Extra Milk      (xem)   0,30 CHF
Foamed Milk     (xfm)   0,50 CHF
Roasted Coffee  (rcf)   0,90 CHF
========================
Please choose another product with the code(second column) or submit your order(x), cancel(c): 
```

#### II/a non-coffee product: type the code and the quantity
``` bash
Please choose another product with the code(second column) or submit your order(x), cancel(c):
br
Please type the quantity:
2
```

#### II/b coffee product: type the code, then the type of extra and the quantity, you can ignore the further extra selection with 'n' 
``` bash
Please choose another product with the code(second column) or submit your order(x), cancel(c):
sc
You can choose an extra with codes to your coffee product: xem, xfm, rcf or say no(n)
xem
You can choose another coffee extra with valid code: xfm, rcf or say no(n)!
n
Please type the quantity: 
3
```

#### II/c Please choose another product with the code(second column) or submit your order(x), cancel(c): 
You can continue or submit or cancel your order here.

### III. List orders.
``` bash
========================
Coffee Corner
========================
1. Create new order
2. List previous orders
3. Quit (Q)
========================
Please choose with a number: 
2
========================
Order: 0
-------------------------------------------
Bacon roll      (br)    4,50 X 2  9,00 CHF
Small coffee    (sc)    2,50 X 3  7,50 CHF
Extra Milk      (xem)   0,30 X 3  0,90 CHF
-------------------------------------------
Total:                           17,40 CHF 
-------------------------------------------
Discounts:
-------------------------------------------
beverage1snack1              2 X -0,30 CHF
-------------------------------------------
Total with discounts:            16,80 CHF 

Order: 1
-------------------------------------------
Orange Juice    (oj)    3,95 X 3  11,85 CHF
-------------------------------------------
Total:                            11,85 CHF 
-------------------------------------------
Discounts:
-------------------------------------------
beverage5th                   1 X -3,95 CHF
-------------------------------------------
Total with discounts:              7,90 CHF 
========================

Press X to return to the Main Menu!

```
That's it :)
