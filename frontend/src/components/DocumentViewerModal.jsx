import { useEffect, useState } from 'react';
import { X, FileText, Download, Eye, ZoomIn, ZoomOut } from 'lucide-react';
import { Button } from './Button';
import { adminViewDocument } from '../api/admin';
import '../styles/DocumentViewerModal.css';

export function DocumentViewerModal({ docId, documentType, onClose }) {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [fileUrl, setFileUrl] = useState('');
  const [fileType, setFileType] = useState('');
  const [imageZoom, setImageZoom] = useState(100);

  useEffect(() => {
    const loadDocument = async () => {
      try {
        const doc = await adminViewDocument(docId);
        
        // Fetch the document with authorization header
        const token = localStorage.getItem('finflow_token');
        const response = await fetch(`/documents/${docId}/download`, {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });
        
        if (!response.ok) {
          throw new Error('Failed to fetch document');
        }
        
        const blob = await response.blob();
        const objectUrl = URL.createObjectURL(blob);
        setFileUrl(objectUrl);
        
        // Determine file type from document type or filename
        const docType = doc.documentType || '';
        const fileName = doc.fileUrl || '';
        
        if (fileName.match(/\.(jpg|jpeg|png|gif|webp)$/i) || docType.includes('PHOTO')) {
          setFileType('image');
        } else if (fileName.match(/\.pdf$/i) || docType.includes('PDF')) {
          setFileType('pdf');
        } else {
          setFileType('other');
        }
      } catch (err) {
        setError('Failed to load document');
        console.error(err);
      } finally {
        setLoading(false);
      }
    };

    loadDocument();
    
    // Cleanup: revoke object URL when component unmounts
    return () => {
      if (fileUrl && fileUrl.startsWith('blob:')) {
        URL.revokeObjectURL(fileUrl);
      }
    };
  }, [docId]);

  const handleDownload = async () => {
    if (fileUrl) {
      // If it's already a blob URL, use it directly
      if (fileUrl.startsWith('blob:')) {
        const link = document.createElement('a');
        link.href = fileUrl;
        link.download = `${documentType}_${docId}`;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
      } else {
        // Fetch with authorization and download
        try {
          const token = localStorage.getItem('finflow_token');
          const response = await fetch(fileUrl, {
            headers: {
              'Authorization': `Bearer ${token}`
            }
          });
          const blob = await response.blob();
          const url = URL.createObjectURL(blob);
          const link = document.createElement('a');
          link.href = url;
          link.download = `${documentType}_${docId}`;
          document.body.appendChild(link);
          link.click();
          document.body.removeChild(link);
          URL.revokeObjectURL(url);
        } catch (err) {
          console.error('Download failed:', err);
        }
      }
    }
  };

  const handleZoomIn = () => {
    setImageZoom(prev => Math.min(prev + 25, 200));
  };

  const handleZoomOut = () => {
    setImageZoom(prev => Math.max(prev - 25, 50));
  };

  const handleBackdropClick = (e) => {
    if (e.target === e.currentTarget) {
      onClose();
    }
  };

  return (
    <div className="document-viewer-overlay" onClick={handleBackdropClick}>
      <div className="document-viewer-modal">
        {/* Header */}
        <div className="document-viewer-header">
          <div className="document-viewer-title">
            <FileText className="document-icon" />
            <div>
              <h2 className="document-name">{documentType.replace(/_/g, ' ')}</h2>
              <p className="document-id">Document ID: {docId}</p>
            </div>
          </div>
          <div className="document-viewer-actions">
            {fileUrl && (
              <Button
                size="sm"
                variant="secondary"
                onClick={handleDownload}
                icon={<Download className="w-4 h-4" />}
              >
                Download
              </Button>
            )}
            {fileType === 'image' && (
              <div className="zoom-controls">
                <button onClick={handleZoomOut} className="zoom-btn" title="Zoom Out">
                  <ZoomOut className="w-4 h-4" />
                </button>
                <span className="zoom-level">{imageZoom}%</span>
                <button onClick={handleZoomIn} className="zoom-btn" title="Zoom In">
                  <ZoomIn className="w-4 h-4" />
                </button>
              </div>
            )}
            <button onClick={onClose} className="close-btn" title="Close">
              <X className="w-5 h-5" />
            </button>
          </div>
        </div>

        {/* Content */}
        <div className="document-viewer-content">
          {loading ? (
            <div className="document-viewer-state">
              <div className="loading-spinner"></div>
              <p className="state-text">Loading document...</p>
            </div>
          ) : error ? (
            <div className="document-viewer-state">
              <div className="error-icon">
                <X className="w-8 h-8" />
              </div>
              <p className="state-title error">Error Loading Document</p>
              <p className="state-text">{error}</p>
            </div>
          ) : fileType === 'image' ? (
            <div className="image-viewer">
              <img
                src={fileUrl}
                alt={documentType}
                className="document-image"
                style={{ transform: `scale(${imageZoom / 100})` }}
              />
            </div>
          ) : fileType === 'pdf' ? (
            <div className="pdf-viewer">
              <iframe
                src={fileUrl}
                className="pdf-frame"
                title={documentType}
              />
            </div>
          ) : (
            <div className="document-viewer-state">
              <div className="info-icon">
                <Eye className="w-8 h-8" />
              </div>
              <p className="state-title">Preview Not Available</p>
              <p className="state-text">
                This file type cannot be previewed in the browser
              </p>
              <Button onClick={handleDownload} icon={<Download className="w-4 h-4" />}>
                Download to View
              </Button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
