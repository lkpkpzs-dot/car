#!/bin/bash

cd "$(dirname "$0")/assets/images"

convert -size 60x60 xc:#3182ce -fill white -pointsize 24 -gravity center -draw "text 0,0 '总'" car-total.png 2>/dev/null || echo "ImageMagick not found, using fallback"

if [ ! -f car-total.png ]; then
  python3 -c "
from PIL import Image, ImageDraw, ImageFont
import os

def create_icon(filename, color, text):
    img = Image.new('RGB', (60, 60), color)
    draw = ImageDraw.Draw(img)
    try:
        draw.text((30, 30), text, fill='white', anchor='mm')
    except:
        draw.text((30, 30), text, fill='white')
    img.save(filename)

os.makedirs('assets/images', exist_ok=True)

create_icon('car-total.png', '#3182ce', '总')
create_icon('car-online.png', '#38a169', '在')
create_icon('car-offline.png', '#718096', '离')
create_icon('car-warning.png', '#e53e3e', '警')
create_icon('car-add.png', '#ed8936', '+')
create_icon('home.png', '#666', 'H')
create_icon('home-active.png', '#3182ce', 'H')
create_icon('approve.png', '#666', 'A')
create_icon('approve-active.png', '#3182ce', 'A')
create_icon('scan.png', '#666', 'S')
create_icon('scan-active.png', '#3182ce', 'S')
create_icon('scan-icon.png', '#3182ce', '扫')
create_icon('check-icon.png', '#38a169', '✓')
create_icon('warning-icon.png', '#e53e3e', '!')
create_icon('empty.png', '#ccc', '空')
print('Icons created successfully')
" 2>/dev/null || echo "PIL not available, please install Pillow"
fi

echo "Icon generation complete"
