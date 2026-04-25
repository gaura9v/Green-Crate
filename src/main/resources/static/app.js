// app.js
const API_BASE = 'http://localhost:8081/api';

// Cart ID storage
let cartId = localStorage.getItem('cartId');

// Initialize cart on first load
async function initCart() {
    if (!cartId) {
        try {
            const response = await fetch(`${API_BASE}/cart`, {
                method: 'POST'
            });
            const cart = await response.json();
            cartId = cart.id;
            localStorage.setItem('cartId', cartId);
        } catch (error) {
            console.error('Error creating cart:', error);
        }
    }
}

// Show toast notification
function showToast(message, isError = false) {
    const toast = document.createElement('div');
    toast.className = `toast ${isError ? 'error' : ''}`;
    toast.textContent = message;
    document.body.appendChild(toast);
    setTimeout(() => toast.remove(), 3000);
}

// Get emoji icon based on product name
function getProductIcon(name) {
    const nameLower = name.toLowerCase();
    
    // Fruits
    if (nameLower.includes('apple')) return '🍎';
    if (nameLower.includes('banana')) return '🍌';
    if (nameLower.includes('grape')) return '🍇';
    if (nameLower.includes('strawberry')) return '🍓';
    if (nameLower.includes('orange')) return '🍊';
    if (nameLower.includes('mango')) return '🥭';
    if (nameLower.includes('watermelon')) return '🍉';
    if (nameLower.includes('pineapple')) return '🍍';
    
    // Vegetables
    if (nameLower.includes('tomato')) return '🍅';
    if (nameLower.includes('potato')) return '🥔';
    if (nameLower.includes('onion')) return '🧅';
    if (nameLower.includes('carrot')) return '🥕';
    if (nameLower.includes('broccoli')) return '🥦';
    if (nameLower.includes('spinach')) return '🥬';
    if (nameLower.includes('cucumber')) return '🥒';
    if (nameLower.includes('corn')) return '🌽';
    if (nameLower.includes('pepper')) return '🫑';
    
    // Dairy
    if (nameLower.includes('milk')) return '🥛';
    if (nameLower.includes('egg')) return '🥚';
    if (nameLower.includes('yogurt') || nameLower.includes('curd')) return '🥛';
    if (nameLower.includes('butter')) return '🧈';
    if (nameLower.includes('cheese')) return '🧀';
    
    // Bakery & Grains
    if (nameLower.includes('bread')) return '🍞';
    if (nameLower.includes('rice')) return '🍚';
    if (nameLower.includes('wheat') || nameLower.includes('flour')) return '🌾';
    if (nameLower.includes('oat')) return '🥣';
    if (nameLower.includes('cereal') || nameLower.includes('flakes')) return '🥣';
    
    // Meat
    if (nameLower.includes('chicken')) return '🍗';
    if (nameLower.includes('meat')) return '🥩';
    if (nameLower.includes('fish')) return '🐟';
    
    // Beverages
    if (nameLower.includes('juice')) return '🧃';
    if (nameLower.includes('tea')) return '🍵';
    if (nameLower.includes('coffee')) return '☕';
    if (nameLower.includes('water')) return '💧';
    
    // Oils & Spices
    if (nameLower.includes('oil')) return '🫒';
    if (nameLower.includes('salt')) return '🧂';
    if (nameLower.includes('sugar')) return '🍬';
    if (nameLower.includes('spice') || nameLower.includes('masala')) return '🌶️';
    
    // Snacks
    if (nameLower.includes('chip')) return '🍟';
    if (nameLower.includes('biscuit') || nameLower.includes('cookie')) return '🍪';
    if (nameLower.includes('chocolate')) return '🍫';
    if (nameLower.includes('ice cream')) return '🍦';
    
    // Pasta & Others
    if (nameLower.includes('pasta')) return '🍝';
    if (nameLower.includes('noodle')) return '🍜';
    
    // Default grocery icon
    return '🛒';
}

// Load products from API
async function loadProducts() {
    const grid = document.getElementById('products-grid');
    if (!grid) return;

    grid.innerHTML = '<div class="loading">Loading products...</div>';

    try {
        const response = await fetch(`${API_BASE}/products`);
        let products = await response.json();

        // Check for search query in URL
        const urlParams = new URLSearchParams(window.location.search);
        const searchQuery = urlParams.get('search');
        
        if (searchQuery) {
            const query = searchQuery.toLowerCase();
            products = products.filter(product => 
                product.name.toLowerCase().includes(query) ||
                (product.description && product.description.toLowerCase().includes(query)) ||
                (product.category && product.category.toLowerCase().includes(query))
            );
        }

        if (products.length === 0) {
            grid.innerHTML = '<div class="empty-cart"><h2>No products available</h2><p>Add products using the API to see them here.</p></div>';
            return;
        }

        grid.innerHTML = products.map(product => `
            <div class="product-card">
                <div class="product-image">${getProductIcon(product.name)}</div>
                <div class="product-info">
                    <div class="product-name">${product.name}</div>
                    <div class="product-description">${product.description || 'No description available'}</div>
                    <div class="product-category">${product.category || 'General'}</div>
                    <div class="product-price">₹ ${product.price ? product.price.toFixed(0) : '0'}</div>
                    <button class="add-to-cart-btn" onclick="addToCart(${product.id}, '${product.name}', ${product.price})">
                        Add to Cart
                    </button>
                </div>
            </div>
        `).join('');
    } catch (error) {
        grid.innerHTML = '<div class="empty-cart"><h2>Error loading products</h2><p>Make sure the backend is running.</p></div>';
    }
}

// Add product to cart
async function addToCart(productId, productName, price) {
    await initCart();

    try {
        const response = await fetch(`${API_BASE}/cart/${cartId}/add?productId=${productId}&quantity=1`, {
            method: 'POST'
        });

        if (response.ok) {
            showToast(`✅ ${productName} added to cart!`);
        } else {
            showToast('Error adding to cart', true);
        }
    } catch (error) {
        showToast('Error adding to cart', true);
    }
}

// Load cart items
async function loadCart() {
    const container = document.getElementById('cart-items');
    if (!container) return;

    await initCart();

    container.innerHTML = '<div class="loading">Loading cart...</div>';

    try {
        const response = await fetch(`${API_BASE}/cart/${cartId}`);
        const cart = await response.json();

        if (!cart.items || cart.items.length === 0) {
            container.innerHTML = `
                <div class="empty-cart">
                    <h2>🛒 Your cart is empty</h2>
                    <p>Browse our products and add items to your cart!</p>
                    <a href="products.html" class="checkout-btn" style="margin: 1rem auto;">Shop Now</a>
                </div>
            `;
            document.getElementById('cart-total').textContent = '₹ 0';
            return;
        }

        let total = 0;
        container.innerHTML = cart.items.map(item => {
            const price = item.product?.price || 0;
            const quantity = item.quantity || 1;
            const itemTotal = price * quantity;
            total += itemTotal;
            const icon = getProductIcon(item.product?.name || '');

            return `
                <div class="cart-item">
                    <div class="cart-item-image">${icon}</div>
                    <div class="cart-item-details">
                        <div class="cart-item-name">${item.product?.name || 'Product'}</div>
                        <div class="cart-item-price">₹ ${price.toFixed(0)} each</div>
                    </div>
                    <div class="quantity-controls">
                        <span>Qty: ${quantity}</span>
                    </div>
                    <div class="cart-item-price">₹ ${itemTotal.toFixed(0)}</div>
                    <button class="remove-btn" onclick="removeFromCart(${item.id})">Remove</button>
                </div>
            `;
        }).join('');

        document.getElementById('cart-total').textContent = `₹ ${total.toFixed(0)}`;
    } catch (error) {
        container.innerHTML = '<div class="empty-cart"><h2>Error loading cart</h2></div>';
    }
}

// Remove item from cart
async function removeFromCart(itemId) {
    try {
        const response = await fetch(`${API_BASE}/cart/${cartId}/item/${itemId}`, {
            method: 'DELETE'
        });

        if (response.ok) {
            showToast('Item removed from cart');
            loadCart();
        } else {
            showToast('Error removing item', true);
        }
    } catch (error) {
        showToast('Error removing item', true);
    }
}

// Load orders
async function loadOrders() {
    const container = document.getElementById('orders-list');
    if (!container) return;

    container.innerHTML = '<div class="loading">Loading orders...</div>';

    try {
        const response = await fetch(`${API_BASE}/orders`);
        const orders = await response.json();

        if (orders.length === 0) {
            container.innerHTML = `
                <div class="empty-orders">
                    <h2>📦 No orders yet</h2>
                    <p>Complete a checkout to see your orders here!</p>
                    <a href="products.html" class="checkout-btn" style="margin: 1rem auto;">Start Shopping</a>
                </div>
            `;
            return;
        }

        container.innerHTML = orders.map(order => `
            <div class="order-card">
                <div class="order-header">
                    <span class="order-id">Order #${order.id}</span>
                    <span class="order-status status-completed">Completed</span>
                </div>
                <div class="order-items">
                    ${order.items ? order.items.map(item => `
                        <div class="order-item">
                            <span>${getProductIcon(item.product?.name || '')} ${item.product?.name || 'Product'} x${item.quantity}</span>
                            <span>₹ ${(item.price || 0).toFixed(0)}</span>
                        </div>
                    `).join('') : '<div class="order-item">No items</div>'}
                </div>
                <div class="order-total">Total: ₹ ${(order.totalAmount || 0).toFixed(0)}</div>
            </div>
        `).join('');
    } catch (error) {
        container.innerHTML = '<div class="empty-orders"><h2>Error loading orders</h2></div>';
    }
}

// Checkout
async function checkout() {
    await initCart();

    try {
        const response = await fetch(`${API_BASE}/orders/checkout/${cartId}`, {
            method: 'POST'
        });

        if (response.ok) {
            showToast('✅ Order placed successfully!');
            // Clear cart
            localStorage.removeItem('cartId');
            cartId = null;
            // Create new cart
            await initCart();
            // Reload cart page
            setTimeout(() => window.location.href = 'orders.html', 1000);
        } else {
            showToast('Error placing order', true);
        }
    } catch (error) {
        showToast('Error placing order', true);
    }
}

// Initialize on page load
document.addEventListener('DOMContentLoaded', () => {
    initCart();
});