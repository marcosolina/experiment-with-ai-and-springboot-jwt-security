/**
 * @module main
 *
 * Application entry point. Mounts the React component tree into the DOM,
 * wrapping the {@link App} component with the required providers:
 * - `React.StrictMode` for development warnings
 * - `ChakraProvider` for Chakra UI theming
 * - `BrowserRouter` for client-side routing
 * - `AuthProvider` for authentication context
 */

import React from 'react'
import ReactDOM from 'react-dom/client'
import { ChakraProvider } from '@chakra-ui/react'
import { BrowserRouter } from 'react-router-dom'
import App from './App'
import { AuthProvider } from './context/AuthContext'

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <ChakraProvider>
      <BrowserRouter>
        <AuthProvider>
          <App />
        </AuthProvider>
      </BrowserRouter>
    </ChakraProvider>
  </React.StrictMode>,
)
