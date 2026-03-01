
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Shopping Website</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            background-color: #f4f4f4;
            margin: 0;
            padding: 0;
        }
        header {
            background-color: #333;
            color: #fff;
            padding: 10px 0;
            text-align: center;
        }
        nav {
            background-color: #444;
            padding: 10px 0;
            text-align: center;
        }
        nav a {
            color: #fff;
            text-decoration: none;
            padding: 0 10px;
        }
        .container {
            width: 80%;
            margin: 20px auto;
        }
        .product {
            background-color: #fff;
            padding: 20px;
            border-radius: 8px;
            box-shadow: 0 2px 5px rgba(0,0,0,0.1);
            margin-bottom: 20px;
        }
        .product img {
            width: 100%;
            border-radius: 8px;
        }
        .product h2 {
            margin-top: 0;
        }
    </style>
</head>
<body>
   
        <h1>Welcome : ${itemlist}</h1>
    </header>
  
    <nav>
        <a href="#">Home</a>
        <a href="#">Shop</a>
        <a href="#">About</a>
        <a href="#">Contact</a>
    </nav>
    <form action="additem">
			<input type="submit" value="Add Items">
			</form>
	<form action="itemlist">
			<input type="submit" value="Get Items">
		</form>
    <!-- <div class="container">
        <div class="product">
            <img src="product1.jpg" alt="Product 1">
            <h2>Product 1</h2>
            <p>Description of Product 1</p>
            <p>Price: $20.00</p>
            <button>Add to Cart</button>
        </div>
        <div class="product">
            <img src="product2.jpg" alt="Product 2">
            <h2>Product 2</h2>
            <p>Description of Product 2</p>
            <p>Price: $25.00</p>
            <button>Add to Cart</button>
        </div>
       <!--Add more products as needed 
    </div>-->
</body>
</html>
