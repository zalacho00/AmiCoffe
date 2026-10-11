import React from 'react'
import AdminDashboard from './pages/admin/AdminDashboard'
import KdsDashboard from './pages/kds/KdsDashboard'
import './App.css'

function App() {
  const path = window.location.pathname

  if (path.startsWith('/kds')) {
    return <KdsDashboard />
  }

  return <AdminDashboard />
}

export default App