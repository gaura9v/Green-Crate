from django.contrib import admin
from .models import Product, Cart, CartItem, Order, OrderItem, Feedback


@admin.register(Product)
class ProductAdmin(admin.ModelAdmin):
    list_display = ('id', 'name', 'category', 'price', 'stock_quantity')
    list_filter = ('category',)
    search_fields = ('name', 'description', 'category')


class CartItemInline(admin.TabularInline):
    model = CartItem
    extra = 0


@admin.register(Cart)
class CartAdmin(admin.ModelAdmin):
    list_display = ('id', 'created_at', 'item_count')
    inlines = [CartItemInline]

    def item_count(self, obj):
        return obj.items.count()


class OrderItemInline(admin.TabularInline):
    model = OrderItem
    extra = 0


@admin.register(Order)
class OrderAdmin(admin.ModelAdmin):
    list_display = ('id', 'status', 'total_amount', 'created_at', 'item_count')
    list_filter = ('status', 'created_at')
    search_fields = ('id',)
    inlines = [OrderItemInline]

    def item_count(self, obj):
        return obj.items.count()


@admin.register(Feedback)
class FeedbackAdmin(admin.ModelAdmin):
    list_display = ('id', 'email', 'rating', 'created_at')
    list_filter = ('rating', 'created_at')
    search_fields = ('email', 'comments')
