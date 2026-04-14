/* =========================================================================
   SISTEMA PUNTO FÁCIL - JS ADAPTADO (POS + HISTORIAL/ANULACIONES)
   ========================================================================= */

let carrito = [];
let pagosRealizados = [];
let bancosDisponibles = [];

// --- VARIABLES DE ESTADO PARA CLIENTES Y DOCUMENTOS ---
let clienteSeleccionado = { id: 1, nombre: "CLIENTE GENERICO" };
let idTipoDocSeleccionado = 1;

// Exposición de funciones al objeto window (Indispensable para Thymeleaf)
window.agregarPago = agregarPago;
window.eliminarPago = eliminarPago;
window.finalizarVenta = finalizarVenta;
window.agregarProducto = agregarProducto;
window.eliminarProducto = eliminarProducto;
window.actualizarCantidad = actualizarCantidad;
window.seleccionarCliente = seleccionarCliente;
window.anularVenta = anularVenta; // <--- Nueva función expuesta

document.addEventListener("DOMContentLoaded", function() {
    configurarBuscador();
    configurarBuscadorClientes();
    cargarFavoritos();
    cargarMasVendidos();
    sincronizarBancosDesdeHTML();
    seleccionarPagoPorDefecto();

    // Listener para cambio de Tipo de Documento
    const selectDoc = document.getElementById('selectTipoDocumento');
    if (selectDoc) {
        selectDoc.addEventListener('change', function() {
            idTipoDocSeleccionado = Number(this.value);
            if (idTipoDocSeleccionado === 3 && clienteSeleccionado.id === 1) {
                Swal.fire('Atención', 'Para Crédito Fiscal debe seleccionar un cliente contribuyente.', 'warning');
            }
        });
    }

    // Listener dinámico para Formas de Pago basado en CATEGORÍA
    const selectPago = document.getElementById('selectFormaPago');
    if (selectPago) {
        selectPago.addEventListener('change', function() {
            const contenedorRef = document.getElementById('contenedorReferencia');
            const categoria = this.options[this.selectedIndex].dataset.categoria || "";
            const esEfectivo = (categoria === "EFECTIVO");

            if (contenedorRef) {
                contenedorRef.style.display = esEfectivo ? 'none' : 'block';
                if (!esEfectivo) {
                    setTimeout(() => document.getElementById('selectBanco')?.focus(), 100);
                }
            }
        });
    }

    // Manejo de Enter en inputs
    const idsInputs = ['montoPago', 'refNumero', 'refTitular'];
    idsInputs.forEach(id => {
        document.getElementById(id)?.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                e.preventDefault();
                agregarPago();
            }
        });
    });

    // Atajos de teclado
    document.addEventListener('keydown', (e) => {
        if (e.key === 'F10') { e.preventDefault(); finalizarVenta(); }
        if (e.key === 'F2') { e.preventDefault(); document.getElementById('buscarProducto')?.focus(); }
    });
});

/**
 * FUNCIÓN DE ANULACIÓN: Procesa la baja de la venta y recarga la lista.
 */
function anularVenta(id) {
    Swal.fire({
        title: '¿Anular Venta #' + id + '?',
        text: "Se devolverá el stock a inventario y se ajustará la caja. Esta acción no se puede deshacer.",
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#d33',
        cancelButtonColor: '#3085d6',
        confirmButtonText: 'Sí, anular venta',
        cancelButtonText: 'Cancelar'
    }).then((result) => {
        if (result.isConfirmed) {
            fetch(`/ventas/anular/${id}`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' }
            })
                .then(res => res.json())
                .then(data => {
                    if (data.status === "success") {
                        Swal.fire('¡Anulada!', data.mensaje, 'success')
                            .then(() => location.reload()); // Recarga para actualizar la lista
                    } else {
                        Swal.fire('Error', data.mensaje || 'No se pudo anular', 'error');
                    }
                })
                .catch(err => {
                    console.error("Error al anular:", err);
                    Swal.fire('Error', 'Error de conexión con el servidor', 'error');
                });
        }
    });
}

function sincronizarBancosDesdeHTML() {
    const selectBanco = document.getElementById("selectBanco");
    if (selectBanco) {
        bancosDisponibles = Array.from(selectBanco.options)
            .filter(opt => opt.value !== "")
            .map(opt => ({
                idBanco: Number(opt.value),
                nombreBanco: opt.text
            }));
    }
}

// --- LÓGICA DE CLIENTES ---
function configurarBuscadorClientes() {
    const input = document.getElementById("buscarClienteInput");
    const contenedorBusqueda = document.getElementById("contenedorBusquedaClientes");
    const listaBusqueda = document.getElementById("listaBusquedaClientes");

    input?.addEventListener("input", function() {
        const q = this.value.trim();
        if (q.length < 2) { contenedorBusqueda.style.display = "none"; return; }

        fetch(`/api/clientes/buscar?term=${encodeURIComponent(q)}`)
            .then(res => res.json())
            .then(clientes => {
                if (clientes.length > 0) {
                    contenedorBusqueda.style.display = "block";
                    listaBusqueda.innerHTML = clientes.map(c => `
                        <div class="list-group-item list-group-item-action bg-dark text-white border-secondary cursor-pointer p-2" 
                             onclick="seleccionarCliente(${c.idCliente}, '${c.nombre.replace(/'/g, "\\'")}')">
                            <div class="d-flex justify-content-between align-items-center">
                                <span><i class="bi bi-person-fill me-2 text-info"></i>${c.nombre}</span>
                                <small class="text-secondary">${c.nit || c.dui || 'S/D'}</small>
                            </div>
                        </div>
                    `).join('');
                } else { contenedorBusqueda.style.display = "none"; }
            }).catch(err => console.error("Error clientes:", err));
    });
}

function seleccionarCliente(id, nombre) {
    clienteSeleccionado = { id: id, nombre: nombre };
    const label = document.getElementById("clienteSeleccionadoLabel");
    if (label) label.innerText = nombre.toUpperCase();
    if (document.getElementById("buscarClienteInput")) document.getElementById("buscarClienteInput").value = "";
    document.getElementById("contenedorBusquedaClientes").style.display = "none";
}

// --- PRODUCTOS ---
function configurarBuscador() {
    const input = document.getElementById("buscarProducto");
    const contenedorBusqueda = document.getElementById("contenedorBusqueda");
    const listaBusqueda = document.getElementById("listaBusqueda");
    input?.addEventListener("input", function() {
        const q = this.value.trim();
        if (q.length < 2) { contenedorBusqueda.style.display = "none"; return; }
        fetch(`/productos/buscar?q=${encodeURIComponent(q)}&esPos=true`)
            .then(res => res.json())
            .then(productos => {
                if (productos.length > 0) {
                    contenedorBusqueda.style.display = "block";
                    listaBusqueda.innerHTML = productos.map(p => crearHtmlBoton(p.idProducto, p.nombre, p.precio)).join('');
                } else { contenedorBusqueda.style.display = "none"; }
            });
    });
}

function crearHtmlBoton(id, nombre, precio) {
    const nombreLimpio = nombre.replace(/'/g, "\\'");
    return `<button type="button" class="btn btn-outline-secondary btn-sm producto-sugerido animate__animated animate__fadeIn" 
                onclick="agregarProducto(${id}, '${nombreLimpio}', ${precio})">
                ${nombre} <span class="text-success fw-bold">$${precio.toFixed(2)}</span>
            </button>`;
}

function agregarProducto(id, nombre, precio) {
    const idNum = Number(id);
    let p = carrito.find(x => x.id === idNum);
    if (p) p.cantidad++;
    else carrito.push({ id: idNum, nombre: nombre, precio: parseFloat(precio), cantidad: 1 });
    if(document.getElementById("buscarProducto")) document.getElementById("buscarProducto").value = "";
    document.getElementById("contenedorBusqueda").style.display = "none";
    renderCarrito();
}

function renderCarrito() {
    const tbody = document.getElementById("carrito");
    let total = 0, items = 0;
    if (!tbody) return;
    tbody.innerHTML = "";
    carrito.forEach((p, i) => {
        let sub = p.precio * p.cantidad;
        total += sub; items += p.cantidad;
        tbody.innerHTML += `
            <tr class="border-bottom border-secondary border-opacity-10 align-middle">
                <td class="small fw-bold text-white py-3">${p.nombre.toUpperCase()}<br>
                    <small class="text-secondary">$${p.precio.toFixed(2)} c/u</small></td>
                <td style="width: 80px;">
                    <input type="number" class="form-control form-control-sm text-center bg-dark text-white border-secondary" 
                           value="${p.cantidad}" onchange="actualizarCantidad(${p.id}, this.value)"></td>
                <td class="text-end text-success fw-bold">$${sub.toFixed(2)}</td>
                <td class="text-end"><i class="bi bi-trash text-danger cursor-pointer" onclick="eliminarProducto(${i})"></i></td>
            </tr>`;
    });
    document.getElementById("itemsCount").innerText = `${items} Artículos`;
    document.getElementById("subtotalVenta").innerText = "$" + total.toFixed(2);
    document.getElementById("totalVentaTexto").innerText = "$" + total.toFixed(2);
    document.getElementById("totalVentaTexto").dataset.total = total;
    actualizarVistaPagos();
}

// --- LÓGICA DE PAGOS ---
function agregarPago() {
    const montoInput = document.getElementById("montoPago");
    const selForma = document.getElementById("selectFormaPago");
    const selBanco = document.getElementById("selectBanco");

    const monto = parseFloat(montoInput.value);
    const totalVenta = parseFloat(document.getElementById("totalVentaTexto")?.dataset.total) || 0;
    const pagadoHastaAhora = pagosRealizados.reduce((a, b) => a + b.monto, 0);
    const restante = totalVenta - pagadoHastaAhora;

    if (isNaN(monto) || monto <= 0) return;
    if (totalVenta === 0) return Swal.fire('Aviso', 'El carrito está vacío', 'info');

    const categoria = selForma.options[selForma.selectedIndex].dataset.categoria;
    const esEfectivo = (categoria === "EFECTIVO");

    if (!esEfectivo && !selBanco.value) {
        return Swal.fire({ icon: 'warning', title: 'Información Requerida', text: 'Debe seleccionar un banco.' });
    }

    if (!esEfectivo && monto > (restante + 0.01)) {
        return Swal.fire('Aviso', `Monto excede el saldo ($${restante.toFixed(2)})`, 'warning');
    }

    const nRef = document.getElementById("refNumero")?.value.trim() || "";
    const titular = document.getElementById("refTitular")?.value.trim() || "";

    pagosRealizados.push({
        idFormaPago: Number(selForma.value),
        nombre: selForma.options[selForma.selectedIndex].text,
        categoria: categoria,
        monto: monto,
        referencia: `${nRef} ${titular}`.trim() || "N/A",
        idBanco: esEfectivo ? null : Number(selBanco.value),
        nombreBanco: esEfectivo ? "" : selBanco.options[selBanco.selectedIndex].text
    });

    montoInput.value = "";
    if(document.getElementById("refNumero")) document.getElementById("refNumero").value = "";
    if(document.getElementById("refTitular")) document.getElementById("refTitular").value = "";
    montoInput.focus();
    actualizarVistaPagos();
}

function actualizarVistaPagos() {
    const total = parseFloat(document.getElementById("totalVentaTexto")?.dataset.total) || 0;
    const pagado = pagosRealizados.reduce((a, b) => a + b.monto, 0);
    const divPagos = document.getElementById("listaPagosRealizados");

    if(divPagos) {
        divPagos.innerHTML = "";
        pagosRealizados.forEach((p, i) => {
            const extraInfo = p.nombreBanco ? ` (${p.nombreBanco})` : "";
            const span = document.createElement("span");
            span.className = "badge bg-dark border border-secondary p-2 me-1 mb-1 animate__animated animate__backInDown";
            span.innerHTML = `${p.nombre}${extraInfo}: $${p.monto.toFixed(2)} `;
            const icon = document.createElement("i");
            icon.className = "bi bi-x-circle ms-2 text-danger cursor-pointer";
            icon.onclick = () => window.eliminarPago(i);
            span.appendChild(icon);
            divPagos.appendChild(span);
        });
    }

    if(document.getElementById("totalPagadoTexto")) document.getElementById("totalPagadoTexto").innerText = "$" + pagado.toFixed(2);
    const rest = total - pagado;
    const elRest = document.getElementById("totalRestanteTexto");
    if (elRest) {
        elRest.innerText = (rest <= 0 ? "Vuelto: $" : "Resta: $") + Math.abs(rest).toFixed(2);
        elRest.className = rest <= 0 ? "text-info fw-bold" : "text-danger fw-bold";
    }
}

// --- FINALIZAR VENTA ---
async function finalizarVenta() {
    const total = parseFloat(document.getElementById("totalVentaTexto").dataset.total) || 0;
    const pagado = pagosRealizados.reduce((a, b) => a + b.monto, 0);

    if (carrito.length === 0) return Swal.fire({ icon: 'warning', title: 'Carrito vacío' });
    if (pagado < (total - 0.009)) return Swal.fire({ icon: 'error', title: 'Pago incompleto' });

    const ventaData = {
        idCliente: clienteSeleccionado.id,
        idTipoDoc: idTipoDocSeleccionado,
        nombreTemporal: clienteSeleccionado.nombre,
        detalles: carrito.map(p => ({
            idProducto: Number(p.id),
            cantidad: parseFloat(p.cantidad),
            precioUnitario: parseFloat(p.precio)
        })),
        pagos: pagosRealizados.map(p => ({
            idFormaPago: Number(p.idFormaPago),
            monto: parseFloat(p.monto),
            referencia: p.referencia,
            idBanco: p.idBanco
        }))
    };

    try {
        const resp = await fetch("/ventas/crear", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(ventaData)
        });
        const res = await resp.json();

        if (res.status === "success" || res.idVenta) {
            if (document.getElementById("checkImprimir")?.checked) {
                window.open('/ventas/ticket/' + (res.idVenta || res.data?.idVenta), '_blank');
            }
            Swal.fire({ icon: 'success', title: '¡Venta Realizada!' }).then(() => location.reload());
        } else {
            Swal.fire({ icon: 'error', title: 'Error', text: res.mensaje || "Error al guardar." });
        }
    } catch (e) {
        Swal.fire({ icon: 'error', title: 'Error de Red' });
    }
}

// --- AUXILIARES ---
function actualizarCantidad(id, v) {
    let p = carrito.find(x => x.id === Number(id));
    if(p) p.cantidad = Math.max(0.1, parseFloat(v) || 1);
    renderCarrito();
}
function eliminarProducto(i) { carrito.splice(i, 1); renderCarrito(); }
function eliminarPago(i) { pagosRealizados.splice(i, 1); actualizarVistaPagos(); }

function seleccionarPagoPorDefecto() {
    const sel = document.getElementById("selectFormaPago");
    if (sel) {
        for (let i = 0; i < sel.options.length; i++) {
            if (sel.options[i].text.toUpperCase().includes("EFECTIVO")) {
                sel.selectedIndex = i; break;
            }
        }
    }
}

async function cargarFavoritos() {
    try {
        const res = await fetch('/ventas/favoritos');
        const data = await res.json();
        const lista = document.getElementById("listaFavoritos");
        if(lista) lista.innerHTML = data.map(p => crearHtmlBoton(p.id_producto || p.idProducto, p.nombre, p.precio)).join('');
    } catch (e) {}
}

async function cargarMasVendidos() {
    try {
        const res = await fetch('/ventas/mas-vendidos');
        const data = await res.json();
        const lista = document.getElementById("listaTopVendidos");
        if(lista) lista.innerHTML = data.map(p => crearHtmlBoton(p.id_producto || p.idProducto, p.nombre, p.precio)).join('');
    } catch (e) {}
}