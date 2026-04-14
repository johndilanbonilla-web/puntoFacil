document.addEventListener('DOMContentLoaded', function() {
    const inputBusqueda = document.getElementById('buscarConcepto');
    const datalist = document.getElementById('conceptosList');
    const inputIdProducto = document.getElementById('idProducto');
    const form = document.getElementById('gastoForm');

    // Referencias a los nuevos campos de banco
    const selectFp = document.getElementById('idFormaPago');
    const containerBanco = document.getElementById('containerBanco');
    const selectBanco = document.getElementById('idBanco');

    // 1. Control de visibilidad del Banco
    // Se activa si la forma de pago es transferencia, cheque o tarjeta
    selectFp.addEventListener('change', function() {
        const textoFp = this.options[this.selectedIndex].text.toUpperCase();
        const requiereBanco = textoFp.includes('TRANS') ||
            textoFp.includes('CHEQUE') ||
            textoFp.includes('TARJETA') ||
            textoFp.includes('BANCO');

        if (requiereBanco) {
            containerBanco.style.display = 'block';
            selectBanco.setAttribute('required', 'true');
        } else {
            containerBanco.style.display = 'none';
            selectBanco.removeAttribute('required');
            selectBanco.value = ''; // Limpiamos la selección si se oculta
        }
    });

    // 2. Buscador en tiempo real (Conceptos tipo GASTO)
    inputBusqueda.addEventListener('input', function() {
        const query = this.value;
        if (query.length < 2) return;

        fetch(`/productos/buscar-gastos?term=${encodeURIComponent(query)}`)
            .then(res => res.json())
            .then(data => {
                datalist.innerHTML = '';
                data.forEach(item => {
                    const option = document.createElement('option');
                    option.value = item.nombre;
                    option.dataset.id = item.idProducto;
                    datalist.appendChild(option);
                });
            });
    });

    // 3. Capturar el ID del Producto
    inputBusqueda.addEventListener('change', function() {
        const option = Array.from(datalist.options).find(opt => opt.value === this.value);
        inputIdProducto.value = option ? option.dataset.id : '';
    });

    // 4. Envío del Formulario
    form.addEventListener('submit', function(e) {
        e.preventDefault();

        // Validaciones básicas
        if (!inputIdProducto.value) {
            Swal.fire('Atención', 'Seleccione un concepto válido de la lista', 'warning');
            return;
        }

        const monto = parseFloat(document.getElementById('monto').value);

        // Construcción del DTO incluyendo el banco
        const gastoDTO = {
            idProducto: parseInt(inputIdProducto.value),
            monto: monto,
            descripcion: document.getElementById('descripcion').value,
            idFormaPago: parseInt(selectFp.value),
            // Si el select de banco está oculto o vacío, enviamos null
            idBanco: selectBanco.value ? parseInt(selectBanco.value) : null
        };

        // Confirmación antes de guardar
        Swal.fire({
            title: '¿Confirmar gasto?',
            text: `Se registrará una salida de $${monto.toFixed(2)}`,
            icon: 'question',
            showCancelButton: true,
            confirmButtonText: 'Sí, guardar',
            cancelButtonText: 'Cancelar',
            confirmButtonColor: '#f43f5e'
        }).then((result) => {
            if (result.isConfirmed) {
                enviarGasto(gastoDTO);
            }
        });
    });

    function enviarGasto(dto) {
        fetch('/gastos/guardar', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(dto)
        })
            .then(res => res.json())
            .then(data => {
                if (data.status === 'success') {
                    Swal.fire('¡Guardado!', data.mensaje, 'success')
                        .then(() => window.location.href = '/gastos/lista');
                } else {
                    Swal.fire('Error', data.mensaje, 'error');
                }
            })
            .catch(err => Swal.fire('Error', 'No se pudo conectar con el servidor', 'error'));
    }
});