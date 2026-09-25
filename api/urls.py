from django.urls import re_path
from . import views

urlpatterns = [
    # Product endpoints
    re_path(r'^products/?$', views.product_list_create, name='product-list-create'),
    re_path(r'^products/(?P<pk>\d+)/?$', views.product_detail, name='product-detail'),
    re_path(r'^products/category/(?P<category>[^/]+)/?$', views.products_by_category, name='products-by-category'),

    # Cart endpoints
    re_path(r'^cart/?$', views.cart_create, name='cart-create'),
    re_path(r'^cart/(?P<cart_id>\d+)/?$', views.cart_detail, name='cart-detail'),
    re_path(r'^cart/(?P<cart_id>\d+)/add/?$', views.cart_add_product, name='cart-add-product'),
    re_path(r'^cart/(?P<cart_id>\d+)/item/(?P<item_id>\d+)/?$', views.cart_remove_item, name='cart-remove-item'),

    # Order endpoints
    re_path(r'^orders/?$', views.order_list, name='order-list'),
    re_path(r'^orders/checkout/(?P<cart_id>\d+)/?$', views.order_checkout, name='order-checkout'),
    re_path(r'^orders/(?P<order_id>\d+)/?$', views.order_detail, name='order-detail'),
    re_path(r'^orders/(?P<order_id>\d+)/status/?$', views.order_update_status, name='order-update-status'),

    # Feedback endpoints
    re_path(r'^feedback/?$', views.feedback_list_create, name='feedback-list-create'),
]
