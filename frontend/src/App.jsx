import { useEffect, useMemo, useState } from 'react'
import './App.css'

function App() {
  const [form, setForm] = useState({
    customerName: '',
    product: '',
    quantity: 1,
    total: '',
    cardNumber: '',
    cardHolder: '',
    expirationDate: '',
    cvv: ''
  })

  const [message, setMessage] = useState('')
  const [orders, setOrders] = useState([])
  const [loadingOrders, setLoadingOrders] = useState(true)

  const [filter, setFilter] = useState('')
  const [statusFilter, setStatusFilter] = useState('ALL')
  const [sortField, setSortField] = useState('id')
  const [sortDirection, setSortDirection] = useState('desc')

  const [currentPage, setCurrentPage] = useState(1)
  const pageSize = 5

  const [selectedOrder, setSelectedOrder] = useState(null)

  const handleChange = (e) => {
    const { name, value } = e.target

    setForm({
      ...form,
      [name]: value
    })
  }

  const loadOrders = async () => {
    try {
      setLoadingOrders(true)

      const response = await fetch('http://localhost:8081/api/orders')

      if (!response.ok) {
        throw new Error('Error loading orders')
      }

      const data = await response.json()
      setOrders(data)

    } catch (error) {
      console.error(error)
    } finally {
      setLoadingOrders(false)
    }
  }
  /*Cargado de ordenes*/
  useEffect(() => {
    loadOrders()
  }, [])

  /*Refrescado de la paguina*/
  useEffect(() => {
    const hasPendingOrders = orders.some(
        (order) => order.status === 'PENDING'
    )

    if (!hasPendingOrders) {
      return
    }

    const interval = setInterval(() => {
      loadOrders()
    }, 7000)

    return () => clearInterval(interval)
  }, [orders])

  const handleSubmit = async (e) => {
    e.preventDefault()

    setMessage('Procesando pedido...')

    const request = {
      customerName: form.customerName,
      product: form.product,
      quantity: Number(form.quantity),
      total: Number(form.total),

      card: {
        cardNumber: form.cardNumber,
        cardHolder: form.cardHolder,
        expirationDate: form.expirationDate,
        cvv: form.cvv
      }
    }

    try {
      const response = await fetch('http://localhost:8081/api/orders', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(request)
      })

      if (!response.ok) {
        throw new Error('Error creating order')
      }

      const data = await response.json()

      setMessage(`Pedido #${data.id} creado correctamente`)

      setForm({
        customerName: '',
        product: '',
        quantity: 1,
        total: '',
        cardNumber: '',
        cardHolder: '',
        expirationDate: '',
        cvv: ''
      })

      await loadOrders()

    } catch (error) {
      console.error(error)
      setMessage('Error al crear el pedido')
    }
  }

  const processedOrders = useMemo(() => {
    let result = [...orders]

    if (filter.trim() !== '') {
      const search = filter.toLowerCase()

      result = result.filter((order) =>
          order.customerName.toLowerCase().includes(search) ||
          order.product.toLowerCase().includes(search) ||
          String(order.id).includes(search)
      )
    }

    if (statusFilter !== 'ALL') {
      result = result.filter(
          (order) => order.status === statusFilter
      )
    }

    result.sort((a, b) => {
      let valueA = a[sortField]
      let valueB = b[sortField]

      if (sortField === 'createdAt') {
        valueA = new Date(valueA)
        valueB = new Date(valueB)
      }

      if (valueA < valueB) {
        return sortDirection === 'asc' ? -1 : 1
      }

      if (valueA > valueB) {
        return sortDirection === 'asc' ? 1 : -1
      }

      return 0
    })

    return result
  }, [orders, filter, statusFilter, sortField, sortDirection])

  const totalPages = Math.max(
      1,
      Math.ceil(processedOrders.length / pageSize)
  )

  const startIndex = (currentPage - 1) * pageSize

  const paginatedOrders = processedOrders.slice(
      startIndex,
      startIndex + pageSize
  )

  useEffect(() => {
    setCurrentPage(1)
  }, [filter, statusFilter, sortField, sortDirection])

  return (
      <div className="app">

        <header>
          <h1>Order Payment System</h1>
          <p>Gestión y procesamiento de pedidos</p>
        </header>

        <main>

          <section>
            <h2>Nuevo pedido</h2>

            <form onSubmit={handleSubmit}>

              <input
                  name="customerName"
                  placeholder="Nombre del cliente"
                  value={form.customerName}
                  onChange={handleChange}
                  required
              />

              <input
                  name="product"
                  placeholder="Producto"
                  value={form.product}
                  onChange={handleChange}
                  required
              />

              <input
                  type="number"
                  name="quantity"
                  min="1"
                  placeholder="Cantidad"
                  value={form.quantity}
                  onChange={handleChange}
                  required
              />

              <input
                  type="number"
                  step="0.01"
                  name="total"
                  placeholder="Total"
                  value={form.total}
                  onChange={handleChange}
                  required
              />

              <h3>Información de pago</h3>

              <input
                  name="cardNumber"
                  placeholder="Número de tarjeta"
                  value={form.cardNumber}
                  onChange={handleChange}
                  required
              />

              <input
                  name="cardHolder"
                  placeholder="Nombre en la tarjeta"
                  value={form.cardHolder}
                  onChange={handleChange}
                  required
              />

              <input
                  name="expirationDate"
                  placeholder="MM/YY"
                  value={form.expirationDate}
                  onChange={handleChange}
                  required
              />

              <input
                  type="password"
                  name="cvv"
                  placeholder="CVV"
                  value={form.cvv}
                  onChange={handleChange}
                  required
              />

              <button type="submit">
                Crear pedido
              </button>

            </form>

            {message && <p>{message}</p>}
          </section>

          <section>
            <h2>Historial de pedidos</h2>

            <div className="filters">

              <input
                  placeholder="Buscar por ID, cliente o producto"
                  value={filter}
                  onChange={(e) => setFilter(e.target.value)}
              />

              <select
                  value={statusFilter}
                  onChange={(e) => setStatusFilter(e.target.value)}
              >
                <option value="ALL">Todos los estados</option>
                <option value="PENDING">PENDING</option>
                <option value="PAID">PAID</option>
                <option value="PAYMENT_FAILED">
                  PAYMENT_FAILED
                </option>
              </select>

              <select
                  value={sortField}
                  onChange={(e) => setSortField(e.target.value)}
              >
                <option value="id">ID</option>
                <option value="customerName">Cliente</option>
                <option value="product">Producto</option>
                <option value="total">Total</option>
                <option value="createdAt">Fecha</option>
              </select>

              <select
                  value={sortDirection}
                  onChange={(e) => setSortDirection(e.target.value)}
              >
                <option value="desc">Descendente</option>
                <option value="asc">Ascendente</option>
              </select>

            </div>

            {loadingOrders ? (
                <p>Cargando pedidos...</p>

            ) : paginatedOrders.length === 0 ? (
                <p>No se encontraron pedidos.</p>

            ) : (
                <>
                  <table>
                    <thead>
                    <tr>
                      <th>ID</th>
                      <th>Cliente</th>
                      <th>Producto</th>
                      <th>Cantidad</th>
                      <th>Total</th>
                      <th>Estado</th>
                      <th>Fecha</th>
                      <th>Acción</th>
                    </tr>
                    </thead>

                    <tbody>
                    {paginatedOrders.map((order) => (
                        <tr key={order.id}>
                          <td>{order.id}</td>
                          <td>{order.customerName}</td>
                          <td>{order.product}</td>
                          <td>{order.quantity}</td>
                          <td>₡{Number(order.total).toFixed(2)}</td>
                          <td>{order.status}</td>
                          <td>
                            {new Date(order.createdAt).toLocaleString()}
                          </td>
                          <td>
                            <button
                                type="button"
                                onClick={() => setSelectedOrder(order)}
                            >
                              Ver detalle
                            </button>
                          </td>
                        </tr>
                    ))}
                    </tbody>
                  </table>

                  <div className="pagination">

                    <button
                        disabled={currentPage === 1}
                        onClick={() =>
                            setCurrentPage(currentPage - 1)
                        }
                    >
                      Anterior
                    </button>

                    <span>
                  Página {currentPage} de {totalPages}
                </span>

                    <button
                        disabled={currentPage === totalPages}
                        onClick={() =>
                            setCurrentPage(currentPage + 1)
                        }
                    >
                      Siguiente
                    </button>

                  </div>
                </>
            )}

          </section>

          {selectedOrder && (
              <section className="order-detail">

                <h2>Detalle del pedido #{selectedOrder.id}</h2>

                <p>
                  <strong>Cliente:</strong>{' '}
                  {selectedOrder.customerName}
                </p>

                <p>
                  <strong>Producto:</strong>{' '}
                  {selectedOrder.product}
                </p>

                <p>
                  <strong>Cantidad:</strong>{' '}
                  {selectedOrder.quantity}
                </p>

                <p>
                  <strong>Total:</strong>{' '}
                  ₡{Number(selectedOrder.total).toFixed(2)}
                </p>

                <p>
                  <strong>Estado:</strong>{' '}
                  {selectedOrder.status}
                </p>

                <p>
                  <strong>Fecha:</strong>{' '}
                  {new Date(
                      selectedOrder.createdAt
                  ).toLocaleString()}
                </p>

                <button
                    type="button"
                    onClick={() => setSelectedOrder(null)}
                >
                  Cerrar
                </button>

              </section>
          )}

        </main>

      </div>
  )
}

export default App