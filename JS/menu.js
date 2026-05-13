const PRODUCTS_API_URL = "http://localhost:8080/api/productos";
const categoryConfig = {
    piqueos: {
        title: "Piqueos & Entradas",
        description: "Entradas ligeras para abrir el apetito."
    },
    sandwiches: {
        title: "Sándwiches",
        description: "Opciones rápidas y sabrosas para cualquier momento."
    },
    fondos: {
        title: "Platos de Fondo",
        description: "Nuestros platos principales con sazón marina y criolla."
    },
    postres: {
        title: "Postres",
        description: "Dulces para cerrar tu experiencia con broche de oro."
    },
    bebidas: {
        title: "Bebidas",
        description: "Refrescos y bebidas para acompañar tu pedido."
    }
};
const menuData = {};

const cart = new Map();
const formatter = new Intl.NumberFormat("es-PE", {
    style: "currency",
    currency: "PEN",
    minimumFractionDigits: 2
});

const menuModalElement = document.getElementById("menuModal");
const menuItemsContainer = document.getElementById("menuItems");
const cartItemsContainer = document.getElementById("cartItems");
const cartTotalElement = document.getElementById("cartTotal");
const menuModalLabel = document.getElementById("menuModalLabel");
const menuModalDescription = document.getElementById("menuModalDescription");
const submitOrderButton = document.getElementById("submitOrderButton");
const menuStatusElement = document.getElementById("menuStatus");

const menuModal = menuModalElement ? new bootstrap.Modal(menuModalElement) : null;
const categoryButtons = document.querySelectorAll(".categoria-btn");

const normalizeText = (value) =>
    String(value || "")
        .normalize("NFD")
        .replace(/[\u0300-\u036f]/g, "")
        .trim()
        .toLowerCase();

const resetMenuData = () => {
    Object.keys(menuData).forEach((key) => delete menuData[key]);
    Object.entries(categoryConfig).forEach(([key, config]) => {
        menuData[key] = {
            ...config,
            items: []
        };
    });
};

const mapCategoryKey = (categoryName) => {
    const normalizedCategory = normalizeText(categoryName);

    if (normalizedCategory.includes("piqueo") || normalizedCategory.includes("entrada")) {
        return "piqueos";
    }
    if (normalizedCategory.includes("sandwich")) {
        return "sandwiches";
    }
    if (normalizedCategory.includes("fondo") || normalizedCategory.includes("plato")) {
        return "fondos";
    }
    if (normalizedCategory.includes("postre")) {
        return "postres";
    }
    if (normalizedCategory.includes("bebida")) {
        return "bebidas";
    }

    return "";
};

const updateCategoryButtonsUI = () => {
    categoryButtons.forEach((button) => {
        const categoryKey = button.dataset.category;
        const category = menuData[categoryKey];
        const hasItems = Boolean(category && category.items.length > 0);
        const labelElement = button.querySelector("span");

        button.disabled = !hasItems;
        button.classList.toggle("disabled", !hasItems);

        if (labelElement && category) {
            labelElement.textContent = category.title;
        }
    });
};

const setMenuStatus = (message, isError = false) => {
    if (!menuStatusElement) return;

    menuStatusElement.textContent = message;
    menuStatusElement.classList.toggle("text-danger", isError);
};

const calculateCartTotal = () =>
    Array.from(cart.values()).reduce(
        (sum, item) => sum + item.price * (item.quantity || 1),
        0
    );

const mapProductsToMenuData = (products) => {
    resetMenuData();

    products
        .filter((product) => product && product.disponible)
        .forEach((product) => {
            const categoryName = product.categoria && product.categoria.nombre;
            const categoryKey = mapCategoryKey(categoryName);

            if (!categoryKey || !menuData[categoryKey]) return;

            menuData[categoryKey].items.push({
                id: product.id,
                name: product.nombre,
                description: product.descripcion || "",
                price: Number(product.precio) || 0
            });
        });
};

const loadMenuData = async () => {
    try {
        const response = await fetch(PRODUCTS_API_URL);
        if (!response.ok) {
            throw new Error(`Error ${response.status}: no se pudo cargar el menú`);
        }

        const products = await response.json();
        if (!Array.isArray(products)) {
            throw new Error("Respuesta inválida del backend");
        }

        mapProductsToMenuData(products);
        updateCategoryButtonsUI();
        setMenuStatus("Menú actualizado.");
    } catch (error) {
        console.error("Error al cargar productos del menú:", error);
        resetMenuData();
        updateCategoryButtonsUI();
        setMenuStatus("No se pudo cargar el menú en este momento.", true);
    }
};

const updateCartUI = () => {
    cartItemsContainer.innerHTML = "";

    if (cart.size === 0) {
        const emptyItem = document.createElement("li");
        emptyItem.className = "text-muted";
        emptyItem.textContent = "Aún no agregas platos.";
        cartItemsContainer.appendChild(emptyItem);
    } else {
        cart.forEach((item) => {
            const listItem = document.createElement("li");
            listItem.className = "d-flex justify-content-between align-items-center mb-2";
            listItem.innerHTML = `
                <span>${item.name}</span>
                <strong class="menu-price">${formatter.format(item.price)}</strong>
            `;
            cartItemsContainer.appendChild(listItem);
        });
    }

    cartTotalElement.textContent = formatter.format(calculateCartTotal());
};

const renderMenuItems = (categoryKey) => {
    const category = menuData[categoryKey];
    if (!category || category.items.length === 0) return;

    menuModalLabel.textContent = category.title;
    menuModalDescription.textContent = category.description;
    menuItemsContainer.innerHTML = "";

    category.items.forEach((item) => {
        const wrapper = document.createElement("label");
        wrapper.className = "menu-item";
        wrapper.innerHTML = `
            <div>
                <h6>${item.name}</h6>
                <p>${item.description}</p>
            </div>
            <div class="d-flex align-items-center gap-3">
                <span class="menu-price">${formatter.format(item.price)}</span>
                <input class="form-check-input" type="checkbox" data-item-id="${item.id}">
            </div>
        `;
        const checkbox = wrapper.querySelector("input");
        checkbox.checked = cart.has(item.id);
        checkbox.addEventListener("change", (event) => {
            if (event.target.checked) {
                cart.set(item.id, {
                    ...item,
                    quantity: 1
                });
            } else {
                cart.delete(item.id);
            }
            updateCartUI();
        });
        menuItemsContainer.appendChild(wrapper);
    });

    menuModal.show();
};

categoryButtons.forEach((button) => {
    button.addEventListener("click", () => {
        const categoryKey = button.dataset.category;
        renderMenuItems(categoryKey);
    });
});

const buildOrderPayload = () => ({
    items: Array.from(cart.values()).map((item) => ({
        productoId: item.id,
        cantidad: item.quantity || 1,
        precioUnitario: item.price,
        subtotal: item.price * (item.quantity || 1)
    })),
    total: calculateCartTotal()
});

const submitOrder = async () => {
    if (cart.size === 0) {
        alert("Agrega al menos un plato antes de enviar el pedido.");
        return;
    }

    const payload = buildOrderPayload();

    try {
        // agregar conexion con backend aqui: enviar pedido a Spring Boot
        // await fetch("/api/pedidos", { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify(payload) });
        // agregar conexion con backend aqui: manejar respuesta (id de pedido, estado y tiempo estimado)
        console.log("Pedido listo para enviar:", payload);
        alert("Pedido listo para enviar. (Pendiente integración backend)");
    } catch (error) {
        console.error("Error al enviar pedido", error);
        alert("No se pudo enviar el pedido. Intenta nuevamente.");
    }
};

if (submitOrderButton) {
    submitOrderButton.addEventListener("click", submitOrder);
}

updateCartUI();
resetMenuData();
updateCategoryButtonsUI();
loadMenuData();
