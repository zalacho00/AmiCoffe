import React, { useState } from 'react';

export default function AdminInventario() {
  const [activeCategory, setActiveCategory] = useState('todas');
  const [searchTerm, setSearchTerm] = useState('');
  const [showAuditoriaModal, setShowAuditoriaModal] = useState(false);

  const [products, setProducts] = useState([
    { id: 'prod_1', name: 'Caramel Macchiato', category: 'Cafés Calientes', price: 11500, stock: 45, isPopular: true, status: 'Disponible', disponible: true },
    { id: 'prod_2', name: 'Nitro Cold Brew & Vanilla', category: 'Cold Brew & Iced Coffee', price: 12500, stock: 12, isPopular: true, status: 'Bajo Stock', disponible: true },
    { id: 'prod_3', name: 'Espresso Doble', category: 'Cafés Calientes', price: 6500, stock: 80, isPopular: false, status: 'Disponible', disponible: true },
    { id: 'prod_4', name: 'Capuchino Vainilla', category: 'Cafés Calientes', price: 9800, stock: 28, isPopular: false, status: 'Disponible', disponible: true },
    { id: 'prod_5', name: 'Iced Shakerato', category: 'Cold Brew & Iced Coffee', price: 10500, stock: 0, isPopular: false, status: 'Agotado', disponible: false },
    { id: 'prod_6', name: 'Croissant Brunch', category: 'Panadería & Brunch', price: 7000, stock: 18, isPopular: false, status: 'Disponible', disponible: true },
  ]);

  // Colección local de auditoria_inventario (HU-07 MVP)
  const [auditoriaLogs, setAuditoriaLogs] = useState([
    { id: 'aud_1', producto_id: 'prod_5', nombre_producto: 'Iced Shakerato', tipo_cambio: 'ajuste_manual', stock_anterior: 5, stock_nuevo: 0, administrador: 'admin@funlam.edu.co', fecha: '2026-09-25 10:30' },
    { id: 'aud_2', producto_id: 'prod_2', nombre_producto: 'Nitro Cold Brew', tipo_cambio: 'ajuste_manual', stock_anterior: 20, stock_nuevo: 12, administrador: 'admin@funlam.edu.co', fecha: '2026-09-25 09:15' }
  ]);

  const categories = [
    { id: 'todas', label: 'Todas las Categorías', count: products.length },
    { id: 'Cafés Calientes', label: '☕ Cafés Calientes', count: 3 },
    { id: 'Cold Brew & Iced Coffee', label: '❄️ Cold Brew & Iced', count: 2 },
    { id: 'Panadería & Brunch', label: '🥐 Panadería & Brunch', count: 1 },
  ];

  // Simulación del llamado a la Cloud Function `actualizarStock` (HU-07)
  const handleStockChange = (id, delta) => {
    const target = products.find(p => p.id === id);
    if (!target) return;

    const nuevoStockCalculado = target.stock + delta;

    // 🟢 Criterio HU-07: Si el stock nuevo es negativo, la Cloud Function rechaza antes de escribir nada.
    if (nuevoStockCalculado < 0) {
      alert("❌ Operación rechazada por Cloud Function actualizarStock: El stock no puede ser negativo.");
      return;
    }

    let nuevoEstado = 'Disponible';
    let estaDisponible = true;

    if (nuevoStockCalculado === 0) {
      nuevoEstado = 'Agotado';
      estaDisponible = false; // 🟢 Se oculta en app de estudiante
    } else if (nuevoStockCalculado <= 15) {
      nuevoEstado = 'Bajo Stock';
    }

    // 🟢 Simulación de escritura atómica en runTransaction
    setProducts(products.map(p => {
      if (p.id === id) {
        return {
          ...p,
          stock: nuevoStockCalculado,
          status: nuevoEstado,
          disponible: estaDisponible
        };
      }
      return p;
    }));

    // Registra en auditoria_inventario
    const newLog = {
      id: `aud_${Date.now()}`,
      producto_id: id,
      nombre_producto: target.name,
      tipo_cambio: delta > 0 ? 'incremento' : 'decremento',
      stock_anterior: target.stock,
      stock_nuevo: nuevoStockCalculado,
      administrador: 'admin@amicoffee.funlam.edu.co',
      fecha: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) + ' hoy'
    };

    setAuditoriaLogs([newLog, ...auditoriaLogs]);
  };

  const handleTogglePopular = (id) => {
    setProducts(products.map(p => p.id === id ? { ...p, isPopular: !p.isPopular } : p));
  };

  const filteredProducts = products.filter(p => {
    const matchesCategory = activeCategory === 'todas' || p.category === activeCategory;
    const matchesSearch = p.name.toLowerCase().includes(searchTerm.toLowerCase());
    return matchesCategory && matchesSearch;
  });

  return (
    <div className="admin-page">
      <div className="page-header-title">
        <div>
          <h2>Gestión de Inventario en Tiempo Real (HU-07)</h2>
          <p className="subtext">
            Los productos con stock 0 se marcan como <strong>Agotado</strong> y se ocultan automáticamente en la app de los estudiantes.
          </p>
        </div>
        <div style={{ display: 'flex', gap: '10px', alignItems: 'center' }}>
          <button className="btn-audit" onClick={() => setShowAuditoriaModal(true)}>
            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
              <polyline points="14 2 14 8 20 8"/>
              <line x1="16" y1="13" x2="8" y2="13"/>
              <line x1="16" y1="17" x2="8" y2="17"/>
              <polyline points="10 9 9 9 8 9"/>
            </svg>
            Historial de Auditoría
          </button>
          <button className="btn-primary btn-new-product">
            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
              <line x1="12" y1="5" x2="12" y2="19"/>
              <line x1="5" y1="12" x2="19" y2="12"/>
            </svg>
            Nuevo Producto
          </button>
        </div>
      </div>

      {/* Categorías estilo AmiCoffee Pills */}
      <div className="category-pills-scroll">
        {categories.map(cat => (
          <button
            key={cat.id}
            className={`category-pill ${activeCategory === cat.id ? 'active' : ''}`}
            onClick={() => setActiveCategory(cat.id)}
          >
            {cat.label}
            <span className="pill-badge">{cat.count}</span>
          </button>
        ))}
      </div>

      {/* Filter and Search Bar */}
      <div className="search-filter-bar">
        <div className="search-input-wrapper">
          <span className="search-icon">🔍</span>
          <input
            type="text"
            placeholder="Buscar producto por nombre..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>
      </div>

      {/* Product Table */}
      <div className="table-card">
        <table className="admin-table">
          <thead>
            <tr>
              <th>Producto</th>
              <th>Categoría</th>
              <th>Precio (COP)</th>
              <th>Destacado</th>
              <th>Control Stock (actualizarStock)</th>
              <th>Estado & Visibilidad App</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            {filteredProducts.map(product => (
              <tr key={product.id}>
                <td>
                  <div className="product-item-cell">
                    <div className="product-mini-icon">☕</div>
                    <div>
                      <strong className="product-item-name">{product.name}</strong>
                      <span style={{ display: 'block', fontSize: '11px', color: '#94a3b8' }}>ID: {product.id}</span>
                    </div>
                  </div>
                </td>
                <td><span className="cat-tag">{product.category}</span></td>
                <td><strong>${product.price.toLocaleString('es-CO')} COP</strong></td>
                <td>
                  <button 
                    className={`star-toggle ${product.isPopular ? 'active' : ''}`}
                    onClick={() => handleTogglePopular(product.id)}
                  >
                    🔥 {product.isPopular ? ' popular' : ' Normal'}
                  </button>
                </td>
                <td>
                  <div className="stock-controls">
                    <button className="stock-btn" onClick={() => handleStockChange(product.id, -1)} title="-1 unidad">-</button>
                    <span className="stock-number">{product.stock} u.</span>
                    <button className="stock-btn" onClick={() => handleStockChange(product.id, 1)} title="+1 unidad">+</button>
                  </div>
                </td>
                <td>
                  <span className={`status-badge ${product.status.toLowerCase().replace(' ', '-')}`}>
                    {product.status}
                  </span>
                  {!product.disponible && (
                    <span style={{ display: 'block', fontSize: '10px', color: '#ef4444', marginTop: '2px', fontWeight: 'bold' }}>
                      🚫 Oculto en App Estudiante
                    </span>
                  )}
                </td>
                <td>
                  <button className="btn-icon" title="Editar">✏️</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Modal o Vista Directa de la Colección auditoria_inventario (HU-07 MVP) */}
      {showAuditoriaModal && (
        <div className="auth-overlay" onClick={() => setShowAuditoriaModal(false)}>
          <div className="auth-card" style={{ maxWidth: '650px' }} onClick={e => e.stopPropagation()}>
            <div className="auth-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <div>
                <h3 style={{ color: '#fff', fontSize: '20px' }}>Historial auditoria_inventario</h3>
                <span style={{ fontSize: '11px', color: '#ffedd5' }}>Transacciones atómicas registradas por la Cloud Function</span>
              </div>
              <button onClick={() => setShowAuditoriaModal(false)} style={{ background: 'none', border: 'none', color: '#fff', fontSize: '20px', cursor: 'pointer' }}>✖</button>
            </div>
            <div className="auth-body">
              <table className="admin-table">
                <thead>
                  <tr>
                    <th>Fecha</th>
                    <th>Producto</th>
                    <th>Tipo</th>
                    <th>Anterior</th>
                    <th>Nuevo</th>
                    <th>Admin (Token)</th>
                  </tr>
                </thead>
                <tbody>
                  {auditoriaLogs.map(log => (
                    <tr key={log.id}>
                      <td><span style={{ fontSize: '11px' }}>{log.fecha}</span></td>
                      <td><strong>{log.nombre_producto}</strong></td>
                      <td><span className="cat-tag">{log.tipo_cambio}</span></td>
                      <td>{log.stock_anterior}</td>
                      <td><strong style={{ color: 'var(--funlam-orange)' }}>{log.stock_nuevo}</strong></td>
                      <td><span style={{ fontSize: '11px', color: '#64748b' }}>{log.administrador}</span></td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
