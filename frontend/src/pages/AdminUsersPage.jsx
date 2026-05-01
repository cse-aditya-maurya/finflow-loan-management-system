import React, { useState, useEffect } from 'react';
import { Users, Search, AlertCircle } from 'lucide-react';
import { adminGetAllUsers, adminGetAllApplications } from '../api/admin';
import './AdminUsersPage.css';

export function AdminUsersPage() {
  const [users, setUsers] = useState([]);
  const [applications, setApplications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [searchTerm, setSearchTerm] = useState('');

  // Fetch users and applications from backend
  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    setLoading(true);
    setError('');
    try {
      // Check if user is logged in and is admin
      const token = localStorage.getItem('finflow_token');
      const userStr = localStorage.getItem('finflow_user');
      
      if (!token) {
        setError('Please login to continue');
        setLoading(false);
        return;
      }
      
      if (userStr) {
        const user = JSON.parse(userStr);
        if (user.role !== 'ADMIN') {
          setError('Access Denied: Admin only');
          setLoading(false);
          return;
        }
      }
      
      // Fetch both users and applications
      const [usersData, appsData] = await Promise.all([
        adminGetAllUsers(),
        adminGetAllApplications().catch(() => [])
      ]);
      
      // Count applications per user
      const appCountByUser = {};
      appsData.forEach(app => {
        appCountByUser[app.userId] = (appCountByUser[app.userId] || 0) + 1;
      });
      
      // Add application count to each user
      const usersWithAppCount = usersData.map(user => ({
        ...user,
        applicationCount: appCountByUser[user.id] || 0
      }));
      
      setUsers(usersWithAppCount);
      setApplications(appsData);
    } catch (err) {
      console.error('Error fetching users:', err);
      if (err.response?.status === 403) {
        setError('Access Denied: Admin privileges required');
      } else {
        setError('Failed to load users. Please try again.');
      }
    } finally {
      setLoading(false);
    }
  };

  // Calculate user statistics
  const getUserStats = () => {
    const totalUsers = users.length;
    const activeUsers = users.filter(u => (u.status === 'ACTIVE' || !u.status)).length;
    const adminUsers = users.filter(u => u.role === 'ADMIN').length;
    const regularUsers = users.filter(u => u.role === 'USER' || u.role !== 'ADMIN').length;
    
    return {
      total: totalUsers,
      active: activeUsers,
      admins: adminUsers,
      regular: regularUsers
    };
  };

  const userStats = getUserStats();

  // Filter users based on search
  const filteredUsers = users.filter(user =>
    user.name?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    user.email?.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <div className="admin-users-page">
      <div className="users-container">
        {/* Header */}
        <div className="users-header">
          <div>
            <h1 className="users-title">
              <Users size={24} style={{ display: 'inline', marginRight: '0.5rem', verticalAlign: 'middle' }} />
              User Management
            </h1>
            <p className="users-subtitle">
              {userStats.total} total · {userStats.active} active · {userStats.admins} admins · {userStats.regular} users · {applications.length} applications
            </p>
          </div>

          {/* Search */}
          <div className="users-search">
            <div style={{ position: 'relative', flex: 1 }}>
              <Search size={16} style={{ position: 'absolute', left: '1rem', top: '50%', transform: 'translateY(-50%)', color: '#8891a8' }} />
              <input
                type="text"
                placeholder="Search users..."
                className="search-input"
                style={{ paddingLeft: '2.75rem' }}
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
              />
            </div>
          </div>
        </div>

        {/* Users Table */}
        {error && (
          <div style={{ background: 'rgba(239, 68, 68, 0.1)', border: '1px solid rgba(239, 68, 68, 0.3)', borderRadius: '12px', padding: '1rem', marginBottom: '1.5rem', color: '#f87171', display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
            <AlertCircle size={20} />
            <span>{error}</span>
          </div>
        )}
        {loading ? (
          <div className="loading-state">
            <div className="spinner"></div>
            <p>Loading users...</p>
          </div>
        ) : filteredUsers.length === 0 ? (
          <div className="empty-state">
            <div className="empty-state-icon">👥</div>
            <p>No users found</p>
          </div>
        ) : (
          <div className="users-table-card">
            <table className="users-table">
              <thead>
                <tr>
                  <th>User</th>
                  <th>Role</th>
                  <th>Status</th>
                  <th>Applications</th>
                  <th>Joined</th>
                </tr>
              </thead>
              <tbody>
                {filteredUsers.map((user) => (
                  <tr key={user.id}>
                    <td data-label="User">
                      <div className="user-info">
                        <div className="user-avatar">
                          {user.name[0].toUpperCase()}
                        </div>
                        <div className="user-details">
                          <div className="user-name">{user.name}</div>
                          <div className="user-email">{user.email}</div>
                        </div>
                      </div>
                    </td>
                    <td data-label="Role">
                      <span className={`role-badge ${user.role.toLowerCase()}`}>
                        {user.role}
                      </span>
                    </td>
                    <td data-label="Status">
                      <span className={`status-badge ${(user.status || 'active').toLowerCase()}`}>
                        <span style={{ width: 6, height: 6, borderRadius: '50%', background: (user.status === 'ACTIVE' || !user.status) ? '#22c55e' : '#8891a8' }}></span>
                        {user.status || 'ACTIVE'}
                      </span>
                    </td>
                    <td data-label="Applications">
                      {user.applicationCount || 0}
                    </td>
                    <td data-label="Joined">
                      {user.createdAt ? new Date(user.createdAt).toLocaleDateString('en-IN', { 
                        day: 'numeric', 
                        month: 'short', 
                        year: 'numeric' 
                      }) : 'N/A'}
                    </td>

                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
