import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { ThemeProvider } from './context/ThemeContext';
import { Navbar } from './components/Navbar';

// Pages
import { LandingPage } from './pages/LandingPage';
import { LoginPage } from './pages/LoginPage';
import { SignupPage } from './pages/SignupPage';
import { VerifyOtpPage } from './pages/VerifyOtpPage';
import { ForgotPasswordPage } from './pages/ForgotPasswordPage';
import { UserDashboard } from './pages/UserDashboard';
import { ApplyLoanPage } from './pages/ApplyLoanPage';
import { MyApplicationsPage } from './pages/MyApplicationsPage';
import { ApplicationDetailPage } from './pages/ApplicationDetailPage';
import { AdminDashboard } from './pages/AdminDashboard';
import { AdminApplicationsPage } from './pages/AdminApplicationsPage';
import { AdminApplicationDetailPage } from './pages/AdminApplicationDetailPage';
import { AdminUsersPage } from './pages/AdminUsersPage';
import { AdminReportsPage } from './pages/AdminReportsPage';

// Route Guard: user must be logged in
function PrivateRoute({ children }) {
  const { isLoggedIn } = useAuth();
  return isLoggedIn ? <>{children}</> : <Navigate to="/login" replace />;
}

// Route Guard: user must be admin
function AdminRoute({ children }) {
  const { isLoggedIn, isAdmin } = useAuth();
  if (!isLoggedIn) return <Navigate to="/login" replace />;
  if (!isAdmin) return <Navigate to="/dashboard" replace />;
  return <>{children}</>;
}

// Route Guard: if already logged in, redirect away from auth pages
function PublicRoute({ children }) {
  const { isLoggedIn, isAdmin } = useAuth();
  if (isLoggedIn) return <Navigate to={isAdmin ? '/admin/dashboard' : '/dashboard'} replace />;
  return <>{children}</>;
}

function AppRoutes() {
  const { isLoggedIn } = useAuth();
  
  return (
    <>
      {isLoggedIn && <Navbar />}
      <Routes>
        {/* Public */}
        <Route path="/" element={<LandingPage />} />
        <Route path="/login" element={<PublicRoute><LoginPage /></PublicRoute>} />
        <Route path="/signup" element={<PublicRoute><SignupPage /></PublicRoute>} />
        <Route path="/verify-otp" element={<VerifyOtpPage />} />
        <Route path="/forgot-password" element={<PublicRoute><ForgotPasswordPage /></PublicRoute>} />

        {/* User protected */}
        <Route path="/dashboard" element={<PrivateRoute><UserDashboard /></PrivateRoute>} />
        <Route path="/apply" element={<PrivateRoute><ApplyLoanPage /></PrivateRoute>} />
        <Route path="/my-applications" element={<PrivateRoute><MyApplicationsPage /></PrivateRoute>} />
        <Route path="/application/:id" element={<PrivateRoute><ApplicationDetailPage /></PrivateRoute>} />

        {/* Admin protected */}
        <Route path="/admin/dashboard" element={<AdminRoute><AdminDashboard /></AdminRoute>} />
        <Route path="/admin/applications" element={<AdminRoute><AdminApplicationsPage /></AdminRoute>} />
        <Route path="/admin/applications/:id" element={<AdminRoute><AdminApplicationDetailPage /></AdminRoute>} />
        <Route path="/admin/users" element={<AdminRoute><AdminUsersPage /></AdminRoute>} />
        <Route path="/admin/reports" element={<AdminRoute><AdminReportsPage /></AdminRoute>} />

        {/* Fallback */}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </>
  );
}

function App() {
  return (
    <Router future={{ v7_startTransition: true, v7_relativeSplatPath: true }}>
      <AuthProvider>
        <ThemeProvider>
          <AppRoutes />
        </ThemeProvider>
      </AuthProvider>
    </Router>
  );
}

export default App;
