/* =========================================================================
   LÓGICA DE MANTENIMIENTO - PRODUCTOS (PAGINACIÓN Y FILTROS AJAX)
   Sincronizada con Diseño Dark Custom y Fragmento Unificado
   ========================================================================= */

document.addEventListener('DOMContentLoaded', function() {
    const inputBusqueda = document.getElementById('busqueda');
    const filtroTipo = document.getElementById('filtroTipo');
    // Contenedor que envuelve tabla + paginador
    const contenedorDinamico = document.getElementById('contenedor-dinamico');

    /**
     * Función principal de carga.
     * Reemplaza el bloque completo de datos y paginación.
     */
    window.cambiarPagina = function(p = 0) {
        if (!contenedorDinamico) return;

        // Feedback visual de carga
        contenedorDinamico.style.opacity = '0.5';
        contenedorDinamico.style.pointerEvents = 'none';

        const q = inputBusqueda ? inputBusqueda.value.trim() : "";
        const tipo = filtroTipo ? filtroTipo.value : "";

        const url = `/productos/mantenimiento/fragmento?page=${p}&q=${encodeURIComponent(q)}&tipo=${tipo}`;

        fetch(url)
            .then(r => {
                if (!r.ok) throw new Error("Error en la respuesta del servidor");
                return r.text();
            })
            .then(html => {
                // Insertamos el fragmento completo (Tabla + Paginación)
                contenedorDinamico.innerHTML = html;
                contenedorDinamico.style.opacity = '1';
                contenedorDinamico.style.pointerEvents = 'auto';
            })
            .catch(err => {
                console.error("Error al cargar fragmento:", err);
                contenedorDinamico.style.opacity = '1';
                contenedorDinamico.style.pointerEvents = 'auto';
            });
    };

    // --- LÓGICA DE EVENTOS (DEBOUNCE) ---
    if (inputBusqueda) {
        let timeoutBusqueda;
        inputBusqueda.addEventListener('input', function() {
            clearTimeout(timeoutBusqueda);
            // Reset a página 0 al buscar
            timeoutBusqueda = setTimeout(() => cambiarPagina(0), 300);
        });
    }

    if (filtroTipo) {
        filtroTipo.addEventListener('change', () => cambiarPagina(0));
    }
});

/**
 * Guarda o actualiza un producto vía Fetch API.
 */
async function guardarProducto() {
    const getVal = (id) => document.getElementById(id) ? document.getElementById(id).value.trim() : "";
    const btn = document.getElementById('btnGuardar');

    // Validaciones básicas antes de enviar
    if (!getVal('nombre')) {
        Swal.fire({ icon: 'warning', title: 'Atención', text: 'El nombre es obligatorio', background: '#1e293b', color: '#fff' });
        return;
    }

    if(btn) btn.disabled = true;
    const btnTextContainer = document.querySelector('#btnGuardar span') || btn;
    const originalContent = btnTextContainer.innerHTML;
    btnTextContainer.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span> Guardando...';

    // Construcción del objeto JSON
    const data = {
        idProducto: getVal('idProducto') ? parseInt(getVal('idProducto')) : null,
        nombre: getVal('nombre'),
        idTipoProducto: parseInt(getVal('idTipoProducto')) || 1,
        codigoBarra: getVal('codigoBarra'),
        precio: parseFloat(getVal('precioVenta')) || 0,
        costoUltimo: parseFloat(getVal('costo')) || 0,
        esFavorito: document.getElementById('esFavorito')?.checked ? 1 : 0,
        activo: 1,
        familia: getVal('idFamilia') ? { idFamilia: parseInt(getVal('idFamilia')) } : null,
        inventario: {
            stockMinimo: parseFloat(getVal('stockMinimo')) || 0,
            idSucursal: 1 // Por defecto a sucursal principal
        }
    };

    try {
        const response = await fetch('/productos/guardar', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });

        if (!response.ok) throw new Error(await response.text());
        const res = await response.json();

        if (res.status === "OK") {
            Swal.fire({
                icon: 'success', title: '¡Éxito!', text: 'Producto guardado correctamente',
                background: '#1e293b', color: '#fff', timer: 1500, showConfirmButton: false
            }).then(() => {
                window.location.href = "/productos/mantenimiento";
            });
        }
    } catch (err) {
        Swal.fire({ icon: 'error', title: 'Error', text: err.message, background: '#1e293b', color: '#fff' });
        if(btn) btn.disabled = false;
        btnTextContainer.innerHTML = originalContent;
    }
}

/**
 * Anula lógicamente un producto (activo = 0)
 */
function anularProducto(id) {
    Swal.fire({
        title: '¿Anular este ítem?',
        text: "No se eliminará, pero no aparecerá en el POS ni inventarios.",
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#10b981',
        cancelButtonColor: '#334155',
        confirmButtonText: 'Sí, anular',
        cancelButtonText: 'Cancelar',
        background: '#1e293b', color: '#fff'
    }).then((result) => {
        if (result.isConfirmed) {
            fetch(`/productos/anular/${id}`, { method: 'POST' })
                .then(r => {
                    if(!r.ok) throw new Error("No se pudo completar la operación");
                    return r.text();
                })
                .then(res => {
                    if(res === "OK") {
                        Swal.fire({ icon: 'success', title: 'Anulado', background: '#1e293b', color: '#fff', timer: 1000, showConfirmButton: false });
                        cambiarPagina(0); // Refrescar lista actual
                    }
                })
                .catch(err => Swal.fire({ icon: 'error', title: 'Error', text: err.message, background: '#1e293b', color: '#fff' }));
        }
    });
}