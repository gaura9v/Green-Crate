from .models import Product

SAMPLE_PRODUCTS = [
    {"name": "Fresh Apples", "description": "Crisp and juicy red apples from local farms", "price": 150.00, "category": "Fruits", "stock_quantity": 50},
    {"name": "Milk", "description": "Fresh whole milk, 1 liter pack", "price": 60.00, "category": "Dairy", "stock_quantity": 30},
    {"name": "Whole Wheat Bread", "description": "Freshly baked whole wheat bread", "price": 45.00, "category": "Bakery", "stock_quantity": 25},
    {"name": "Farm Eggs", "description": "Farm fresh eggs, pack of 12", "price": 80.00, "category": "Dairy", "stock_quantity": 40},
    {"name": "Chicken Breast", "description": "Boneless chicken breast, 1kg", "price": 450.00, "category": "Meat", "stock_quantity": 20},
    {"name": "Basmati Rice", "description": "Premium quality basmati rice, 5kg", "price": 350.00, "category": "Grains", "stock_quantity": 15},
    {"name": "Fresh Tomatoes", "description": "Fresh red tomatoes, 1kg", "price": 40.00, "category": "Vegetables", "stock_quantity": 35},
    {"name": "Potatoes", "description": "Fresh potatoes, 5kg bag", "price": 120.00, "category": "Vegetables", "stock_quantity": 25},
    {"name": "Onions", "description": "Fresh yellow onions, 1kg", "price": 35.00, "category": "Vegetables", "stock_quantity": 40},
    {"name": "Carrots", "description": "Fresh organic carrots, 1kg", "price": 50.00, "category": "Vegetables", "stock_quantity": 30},
    {"name": "Greek Yogurt", "description": "Creamy greek yogurt, 500g", "price": 90.00, "category": "Dairy", "stock_quantity": 20},
    {"name": "Butter", "description": "Pure butter, 500g pack", "price": 220.00, "category": "Dairy", "stock_quantity": 15},
    {"name": "Cheddar Cheese", "description": "Aged cheddar cheese, 400g", "price": 350.00, "category": "Dairy", "stock_quantity": 18},
    {"name": "Orange Juice", "description": "Fresh orange juice, 1 liter", "price": 150.00, "category": "Beverages", "stock_quantity": 22},
    {"name": "Green Grapes", "description": "Sweet green grapes, 1kg", "price": 180.00, "category": "Fruits", "stock_quantity": 28},
    {"name": "Strawberries", "description": "Fresh strawberries, 500g", "price": 200.00, "category": "Fruits", "stock_quantity": 25},
    {"name": "Broccoli", "description": "Fresh broccoli, 1kg", "price": 80.00, "category": "Vegetables", "stock_quantity": 30},
    {"name": "Spinach", "description": "Organic spinach leaves, 500g", "price": 45.00, "category": "Vegetables", "stock_quantity": 35},
    {"name": "Cucumber", "description": "Fresh cucumber, 1kg", "price": 30.00, "category": "Vegetables", "stock_quantity": 40},
    {"name": "Corn Flakes", "description": "Kellogg's corn flakes, 375g", "price": 250.00, "category": "Breakfast", "stock_quantity": 20},
    {"name": "Oatmeal", "description": "Quaker oats, 1kg", "price": 280.00, "category": "Breakfast", "stock_quantity": 18},
    {"name": "Pasta", "description": "Barilla penne pasta, 500g", "price": 120.00, "category": "Pasta", "stock_quantity": 25},
    {"name": "Olive Oil", "description": "Extra virgin olive oil, 1L", "price": 650.00, "category": "Oil", "stock_quantity": 15},
    {"name": "Sugar", "description": "Tate & Lyle sugar, 1kg", "price": 55.00, "category": "Baking", "stock_quantity": 30},
    {"name": "Salt", "description": "Tata salt, 1kg", "price": 25.00, "category": "Spices", "stock_quantity": 50},
    {"name": "Tea Bags", "description": "Lipton tea bags, 100 pcs", "price": 180.00, "category": "Beverages", "stock_quantity": 25},
    {"name": "Coffee", "description": "Nescafe instant coffee, 200g", "price": 450.00, "category": "Beverages", "stock_quantity": 15},
    {"name": "Potato Chips", "description": "Lay's classic chips, 150g", "price": 50.00, "category": "Snacks", "stock_quantity": 30},
    {"name": "Cookies", "description": "Parle-G biscuits, 800g", "price": 80.00, "category": "Snacks", "stock_quantity": 22},
]

def load_initial_products():
    """Populates the database with initial products if none exist, matching DataLoader.java"""
    if Product.objects.count() == 0:
        products = [Product(**p) for p in SAMPLE_PRODUCTS]
        Product.objects.bulk_create(products)
        print(f"✅ Sample products loaded: {len(products)} items")
        return len(products)
    return 0
