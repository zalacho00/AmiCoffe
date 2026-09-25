import React, { useState, useEffect } from 'react';
import {
  collection,
  query,
  orderBy,
  onSnapshot,
  doc,
  runTransaction,
  addDoc,
  updateDoc,
  serverTimestamp,
} from 'firebase/firestore';
import { db, auth } from '../../firebase/config';

const CATEGORIAS = [
  { id: 'todas', label: 'Todas las Categorías' },
  { id: 'bebidas', label: '🥤 Bebidas' },
  { id: 'snacks', label: '🥪 Snacks' },
  { id: 'almuerzos', label: '🍽️ Almuerzos' },
];

const UMBRAL_BAJO_STOCK = 5; // solo visual, no cambia el campo `estado` en la BD

export default function AdminInventario() {
  const [productos, setProductos] = useState([]);
  const [auditoriaLogs, setAuditoriaLogs] = useState([]);
  const [activeCategory, setActiveCategory] = useState('todas');
  const [searchTerm, setSearchTerm] = useState('');
  const [showAuditoriaModal, setShowAuditoriaModal] = useState(false);
  const [showFormModal, setShowFormModal] = useState(false);
  const [editingProduct, setEditingProduct] = useState(null); // null = creando nuevo
  const [loading, setLoading] = useState(true);
  const [errorMsg, setErrorMsg] = useState('');

  // ── Escucha en tiempo real de `productos` ──
  useEffect(() => {
    const q = query(collection(db, 'productos'), orderBy('nombre'));
    const unsubscribe = onSnapshot(
      q,
      (snapshot) => {
        setProductos(snapshot.docs.map((d) => ({ id: d.id, ...d.data() })));
        setLoading(false);
      },
      (err) => {
        console.error('Error leyendo productos:', err);
        setErrorMsg('No se pudo cargar el inventario.');
        setLoading(false);
      }
    );
    return () => unsubscribe();
  }, []);

  // ── Escucha en tiempo real de `auditoria_inventario` (últimos 20) ──
  useEffect(() => {
    const q = query(collection(db, 'auditoria_inventario'), orderBy('fecha', 'desc'));
    const unsubscribe = onSnapshot(q, (snapshot) => {
      setAuditoriaLogs(snapshot.docs.slice(0, 20).map((d) => ({ id: d.id, ...d.data() })));
    });
    return () => unsubscribe();
  }, []);

  // ── Cambiar stock (transacción atómica + auditoría) ──
  const handleStockChange = async (producto, delta) => {
    const productoRef = doc(db, 'productos', producto.id);
    const adminEmail = auth.currentUser?.email || 'desconocido';

    try {
      let stockAnterior = 0;
      let stockNuevo = 0;

      await runTransaction(db, async (transaction) => {
        const snap = await transaction.get(productoRef);
        if (!snap.exists()) throw new Error('El producto ya no existe.');

        stockAnterior = snap.data().stock;
        stockNuevo = stockAnterior + delta;

        if (stockNuevo < 0) {
          throw new Error('El stock no puede quedar negativo.');
        }

        const nuevoEstado = stockNuevo === 0 ? 'agotado' : 'disponible';

        transaction.update(productoRef, {
          stock: stockNuevo,
          estado: nuevoEstado,
        });
      });

      // Registro de auditoría (fuera de la transacción, no crítico si falla)
      await addDoc(collection(db, 'auditoria_inventario'), {
        producto_id: producto.id,
        nombre_producto: producto.nombre,
        tipo_cambio: delta > 0 ? 'incremento' : 'decremento',
        stock_anterior: stockAnterior,
        stock_nuevo: stockNuevo,
        administrador: adminEmail,
        fecha: serverTimestamp(),
      });
    } catch (err) {
      console.error(err);
      alert(`❌ ${err.message}`);
    }
  };

  // ── Crear o editar producto (datos, no stock) ──
  const handleSaveProduct = async (formData) => {
    try {
      if (editingProduct) {
        await updateDoc(doc(db, 'productos', editingProduct.id), {
          nombre: formData.nombre,
          categoria: formData.categoria,
          precio: Number(formData.precio),
        });
      } else {
        await addDoc(collection(db, 'productos'), {
          nombre: formData.nombre,
          categoria: formData.categoria,
          precio: Number(formData.precio),
          stock: Number(formData.stock) || 0,
          estado: Number(formData.stock) > 0 ? 'disponible' : 'agotado',
        });
      }
      setShowFormModal(false);
      setEditingProduct(null);
    } catch (err) {
      console.error(err);
      alert('❌ No se pudo guardar el producto.');
    }
  };

  const filteredProducts = productos.filter((p) => {
    const matchesCategory = activeCategory === 'todas' || p.categoria === activeCategory;
    const matchesSearch = p.nombre?.toLowerCase().includes(searchTerm.toLowerCase());
    return matchesCategory && matchesSearch;
  });

  const countByCategory = (catId) =>
    catId === 'todas' ? productos.length : productos.filter((p) => p.categoria === catId).length;

  const formatFecha = (ts) => {
    if (!ts) return '—';
    const date = ts.toDate ? ts.toDate() : new Date(ts);
    return date.toLocaleString('es-CO', { dateStyle: 'short', timeStyle: 'short' });
  };

  if (loading) {
    return <div className="admin-page">Cargando inventario…</div>;
  }

  return (
    <div className="admin-page">
      <div className="page-header-title">
        <div>
          <h2>Gestión de Inventario en Tiempo Real</h2>
          <p className="subtext">
            Los productos con stock 0 se marcan como <strong>Agotado</strong> y se ocultan automáticamente en la app de los estudiantes.
          </p>
        </div>
        <div style={{ display: 'flex', gap: '10px', alignItems: 'center' }}>
          <button className="btn-audit" onClick={() => setShowAuditoriaModal(true)}>
            📋 Historial de Auditoría
          </button>
          <button
            className="btn-primary btn-new-product"
            onClick={() => { setEditingProduct(null); setShowFormModal(true); }}
          >
            + Nuevo Producto
          </button>
        </div>
      </div>

      {errorMsg && <div className="auth-error-alert">{errorMsg}</div>}

      {/* Categorías */}
      <div className="category-pills-scroll">
        {CATEGORIAS.map((cat) => (
          <button
            key={cat.id}
            className={`category-pill ${activeCategory === cat.id ? 'active' : ''}`}
            onClick={() => setActiveCategory(cat.id)}
          >
            {cat.label}
            <span className="pill-badge">{countByCategory(cat.id)}</span>
          </button>
        ))}
      </div>

      {/* Búsqueda */}
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

      {/* Tabla */}
      <div className="table-card">
        <table className="admin-table">
          <thead>
            <tr>
              <th>Producto</th>
              <th>Categoría</th>
              <th>Precio (COP)</th>
              <th>Control Stock</th>
              <th>Estado & Visibilidad App</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            {filteredProducts.length === 0 && (
              <tr>
                <td colSpan={6} style={{ textAlign: 'center', padding: '24px' }}>
                  No hay productos en esta categoría todavía.
                </td>
              </tr>
            )}
            {filteredProducts.map((product) => {
              const bajoStock = product.stock > 0 && product.stock <= UMBRAL_BAJO_STOCK;
              return (
                <tr key={product.id}>
                  <td>
                    <div className="product-item-cell">
                      <div className="product-mini-icon">☕</div>
                      <div>
                        <strong className="product-item-name">{product.nombre}</strong>
                        <span style={{ display: 'block', fontSize: '11px', color: '#94a3b8' }}>
                          ID: {product.id}
                        </span>
                      </div>
                    </div>
                  </td>
                  <td>
                    <span className="cat-tag">
                      {CATEGORIAS.find((c) => c.id === product.categoria)?.label || product.categoria}
                    </span>
                  </td>
                  <td><strong>${Number(product.precio).toLocaleString('es-CO')} COP</strong></td>
                  <td>
                    <div className="stock-controls">
                      <button
                        className="stock-btn"
                        onClick={() => handleStockChange(product, -1)}
                        disabled={product.stock <= 0}
                      >
                        -
                      </button>
                      <span className="stock-number">{product.stock} u.</span>
                      <button className="stock-btn" onClick={() => handleStockChange(product, 1)}>
                        +
                      </button>
                    </div>
                  </td>
                  <td>
                    <span
                      className={`status-badge ${
                        product.estado === 'agotado' ? 'agotado' : bajoStock ? 'bajo-stock' : 'disponible'
                      }`}
                    >
                      {product.estado === 'agotado' ? 'Agotado' : bajoStock ? 'Bajo Stock' : 'Disponible'}
                    </span>
                    {product.estado === 'agotado' && (
                      <span style={{ display: 'block', fontSize: '10px', color: '#ef4444', marginTop: '2px', fontWeight: 'bold' }}>
                        🚫 Oculto en App Estudiante
                      </span>
                    )}
                  </td>
                  <td>
                    <button
                      className="btn-icon"
                      title="Editar"
                      onClick={() => { setEditingProduct(product); setShowFormModal(true); }}
                    >
                      ✏️
                    </button>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>

      {/* Modal de auditoría */}
      {showAuditoriaModal && (
        <div className="auth-overlay" onClick={() => setShowAuditoriaModal(false)}>
          <div className="auth-card" style={{ maxWidth: '650px' }} onClick={(e) => e.stopPropagation()}>
            <div className="auth-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <div>
                <h3 style={{ color: '#fff', fontSize: '20px' }}>Historial auditoria_inventario</h3>
                <span style={{ fontSize: '11px', color: '#ffedd5' }}>Últimos 20 cambios registrados</span>
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
                    <th>Admin</th>
                  </tr>
                </thead>
                <tbody>
                  {auditoriaLogs.length === 0 && (
                    <tr><td colSpan={6} style={{ textAlign: 'center' }}>Sin movimientos todavía.</td></tr>
                  )}
                  {auditoriaLogs.map((log) => (
                    <tr key={log.id}>
                      <td><span style={{ fontSize: '11px' }}>{formatFecha(log.fecha)}</span></td>
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

      {/* Modal de crear/editar producto */}
      {showFormModal && (
        <ProductFormModal
          initialData={editingProduct}
          onClose={() => { setShowFormModal(false); setEditingProduct(null); }}
          onSave={handleSaveProduct}
        />
      )}
    </div>
  );
}

function ProductFormModal({ initialData, onClose, onSave }) {
  const [nombre, setNombre] = useState(initialData?.nombre || '');
  const [categoria, setCategoria] = useState(initialData?.categoria || 'bebidas');
  const [precio, setPrecio] = useState(initialData?.precio || '');
  const [stock, setStock] = useState(initialData?.stock ?? 0);
  const [saving, setSaving] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!nombre || !precio) return;
    setSaving(true);
    await onSave({ nombre, categoria, precio, stock });
    setSaving(false);
  };

  return (
    <div className="auth-overlay" onClick={onClose}>
      <div className="auth-card" style={{ maxWidth: '420px' }} onClick={(e) => e.stopPropagation()}>
        <div className="auth-header">
          <h3 style={{ color: '#fff' }}>{initialData ? 'Editar producto' : 'Nuevo producto'}</h3>
        </div>
        <div className="auth-body">
          <form onSubmit={handleSubmit} className="auth-form">
            <div className="form-group">
              <label>Nombre</label>
              <input value={nombre} onChange={(e) => setNombre(e.target.value)} required />
            </div>
            <div className="form-group">
              <label>Categoría</label>
              <select value={categoria} onChange={(e) => setCategoria(e.target.value)}>
                <option value="bebidas">Bebidas</option>
                <option value="snacks">Snacks</option>
                <option value="almuerzos">Almuerzos</option>
              </select>
            </div>
            <div className="form-group">
              <label>Precio (COP)</label>
              <input type="number" min="0" value={precio} onChange={(e) => setPrecio(e.target.value)} required />
            </div>
            {!initialData && (
              <div className="form-group">
                <label>Stock inicial</label>
                <input type="number" min="0" value={stock} onChange={(e) => setStock(e.target.value)} />
              </div>
            )}
            <button type="submit" className="auth-submit-btn" disabled={saving}>
              {saving ? 'Guardando…' : initialData ? 'Guardar cambios' : 'Crear producto'}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}