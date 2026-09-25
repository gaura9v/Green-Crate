"""
URL configuration for ecommerce_backend project.
"""

from django.contrib import admin
from django.urls import path, include, re_path
from api.views import serve_frontend

urlpatterns = [
    path('admin/', admin.site.urls),
    path('api/', include('api.urls')),
    
    # Root serves home.html
    path('', serve_frontend, kwargs={'path': 'home.html'}, name='frontend-root'),
    
    # Serve static frontend pages and assets (home.html, styles.css, app.js, etc.)
    re_path(r'^(?P<path>.*)$', serve_frontend, name='frontend-static'),
]
