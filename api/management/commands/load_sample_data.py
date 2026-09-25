from django.core.management.base import BaseCommand
from api.data_loader import load_initial_products, SAMPLE_PRODUCTS
from api.models import Product

class Command(BaseCommand):
    help = 'Loads initial sample products into the database if empty, matching Spring Boot DataLoader'

    def add_arguments(self, parser):
        parser.add_argument(
            '--force',
            action='store_true',
            help='Force reload sample products even if products exist',
        )

    def handle(self, *args, **options):
        force = options.get('force', False)
        if force:
            Product.objects.all().delete()
            products = [Product(**p) for p in SAMPLE_PRODUCTS]
            Product.objects.bulk_create(products)
            self.stdout.write(self.style.SUCCESS(f"✅ Force-reloaded {len(products)} sample products!"))
        else:
            loaded = load_initial_products()
            if loaded > 0:
                self.stdout.write(self.style.SUCCESS(f"✅ Successfully loaded {loaded} sample products."))
            else:
                self.stdout.write("Products already exist in database. Use --force to reload.")
