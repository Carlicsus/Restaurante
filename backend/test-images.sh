#!/bin/bash

# ============================================================================
# Script de Prueba: Manejo de Imágenes para Dish
# ============================================================================
# 
# Uso:
#   bash test-images.sh
#
# Requisitos:
#   - curl instalado
#   - Token JWT válido en BEARER_TOKEN
#   - Archivo de imagen en ./test-image.jpg
#

set -e

# Configuración
API_URL="http://localhost:8080/api"
BEARER_TOKEN="YOUR_JWT_TOKEN_HERE"
MENU_TYPE_UUID="32characteruuidstring1234567890ab"
TEST_IMAGE="./test-image.jpg"

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}=== Test: Manejo de Imágenes para Dish ===${NC}\n"

# ============================================================================
# 1. Crear Dish (sin imagen)
# ============================================================================
echo -e "${YELLOW}[1/5] Creando Dish sin imagen...${NC}"

DISH_RESPONSE=$(curl -s -X POST "$API_URL/dish/new" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $BEARER_TOKEN" \
  -d '{
    "name": "Pizza Margherita Test",
    "menuType": "'$MENU_TYPE_UUID'",
    "cost": 12500,
    "description": "Pizza clásica con tomate, mozzarella y albahaca",
    "availableDishes": -1
  }')

echo "Respuesta: $DISH_RESPONSE"

DISH_UUID=$(echo $DISH_RESPONSE | grep -o '"data":"[^"]*' | cut -d'"' -f4)

if [ -z "$DISH_UUID" ]; then
  echo -e "${RED}Error: No se pudo obtener UUID del Dish${NC}"
  exit 1
fi

echo -e "${GREEN}✓ Dish creado con UUID: $DISH_UUID${NC}\n"

# ============================================================================
# 2. Obtener info del Dish (sin imagen)
# ============================================================================
echo -e "${YELLOW}[2/5] Obteniendo info del Dish...${NC}"

curl -s -X GET "$API_URL/dish/$DISH_UUID/info" \
  -H "Authorization: Bearer $BEARER_TOKEN" | jq '.'

echo -e "${GREEN}✓ Info obtenida${NC}\n"

# ============================================================================
# 3. Subir imagen
# ============================================================================
if [ ! -f "$TEST_IMAGE" ]; then
  echo -e "${RED}Error: Archivo $TEST_IMAGE no encontrado${NC}"
  echo -e "${YELLOW}Por favor, proporciona un archivo de imagen llamado test-image.jpg${NC}"
  exit 1
fi

echo -e "${YELLOW}[3/5] Subiendo imagen...${NC}"

UPLOAD_RESPONSE=$(curl -s -X POST "$API_URL/dish/$DISH_UUID/upload-image" \
  -H "Authorization: Bearer $BEARER_TOKEN" \
  -F "image=@$TEST_IMAGE")

echo "Respuesta: $UPLOAD_RESPONSE"

IMAGE_URL=$(echo $UPLOAD_RESPONSE | grep -o '"imageUrl":"[^"]*' | cut -d'"' -f4)

if [ -z "$IMAGE_URL" ]; then
  echo -e "${RED}Error: No se pudo subir la imagen${NC}"
  exit 1
fi

echo -e "${GREEN}✓ Imagen subida: $IMAGE_URL${NC}\n"

# ============================================================================
# 4. Obtener info del Dish (con imagen)
# ============================================================================
echo -e "${YELLOW}[4/5] Obteniendo info actualizada del Dish...${NC}"

curl -s -X GET "$API_URL/dish/$DISH_UUID/info" \
  -H "Authorization: Bearer $BEARER_TOKEN" | jq '.'

echo -e "${GREEN}✓ Info actualizada${NC}\n"

# ============================================================================
# 5. Descargar imagen
# ============================================================================
echo -e "${YELLOW}[5/5] Descargando imagen...${NC}"

FILE_NAME=$(echo $IMAGE_URL | rev | cut -d'/' -f1 | rev)
echo "Nombre de archivo: $FILE_NAME"

curl -s -X GET "$API_URL/images/$FILE_NAME" \
  -o "downloaded-image.jpg"

if [ -f "downloaded-image.jpg" ]; then
  echo -e "${GREEN}✓ Imagen descargada: downloaded-image.jpg${NC}\n"
else
  echo -e "${RED}Error: No se pudo descargar la imagen${NC}"
  exit 1
fi

# ============================================================================
# RESUMEN
# ============================================================================
echo -e "${GREEN}=== ✓ TODOS LOS TESTS PASARON ===${NC}\n"

echo "Resumen:"
echo "  Dish UUID:  $DISH_UUID"
echo "  Image URL:  $IMAGE_URL"
echo ""
echo "Pruebas adicionales que puedes hacer:"
echo ""
echo "1. Eliminar imagen:"
echo "   curl -X DELETE \\$API_URL/dish/$DISH_UUID/image \\"
echo "     -H \"Authorization: Bearer \$BEARER_TOKEN\""
echo ""
echo "2. Obtener lista de Dishes:"
echo "   curl $API_URL/dish/list"
echo ""
echo "3. Ver imagen en navegador:"
echo "   $IMAGE_URL"
echo ""

# ============================================================================
# FUNCIONES DE PRUEBA ADICIONALES (comentadas)
# ============================================================================

# Función para probar eliminación de imagen
# test_delete_image() {
#   echo -e "${YELLOW}Eliminando imagen...${NC}"
#   
#   DELETE_RESPONSE=$(curl -s -X DELETE "$API_URL/dish/$DISH_UUID/image" \
#     -H "Authorization: Bearer $BEARER_TOKEN")
#   
#   echo "Respuesta: $DELETE_RESPONSE"
#   
#   if echo $DELETE_RESPONSE | grep -q '"success":true'; then
#     echo -e "${GREEN}✓ Imagen eliminada${NC}"
#   else
#     echo -e "${RED}Error al eliminar imagen${NC}"
#   fi
# }

# Función para subir imagen inválida (test de error)
# test_invalid_image() {
#   echo -e "${YELLOW}Intentando subir archivo inválido (esperado: error)...${NC}"
#   
#   echo "contenido inválido" > invalid-file.txt
#   
#   INVALID_RESPONSE=$(curl -s -X POST "$API_URL/dish/$DISH_UUID/upload-image" \
#     -H "Authorization: Bearer $BEARER_TOKEN" \
#     -F "image=@invalid-file.txt")
#   
#   echo "Respuesta: $INVALID_RESPONSE"
#   
#   if echo $INVALID_RESPONSE | grep -q '"success":false'; then
#     echo -e "${GREEN}✓ Validación funcionó correctamente${NC}"
#   fi
#   
#   rm invalid-file.txt
# }
