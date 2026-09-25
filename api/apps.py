from django.apps import AppConfig
from django.db.models.signals import post_migrate

def populate_products(sender, **kwargs):
    if sender.name == 'api':
        from .data_loader import load_initial_products
        load_initial_products()

class ApiConfig(AppConfig):
    default_auto_field = 'django.db.models.BigAutoField'
    name = 'api'

    def ready(self):
        post_migrate.connect(populate_products, sender=self)
