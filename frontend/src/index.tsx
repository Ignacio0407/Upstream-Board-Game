import React from 'react';
import ReactDOM from 'react-dom/client';
import './index.css';
import './global.css';
import '@splidejs/react-splide/css/sea-green';
import App from './App.tsx';
import reportWebVitals from './reportWebVitals.ts';
import 'bootstrap/dist/css/bootstrap.min.css';
import { BrowserRouter } from 'react-router-dom';
import { DndProvider } from 'react-dnd'; 
import { HTML5Backend } from 'react-dnd-html5-backend';

const container = document.getElementById('root');
if (!container) throw new Error("Container 'root' not found");
const root = ReactDOM.createRoot(container);
root.render(
  <React.StrictMode>
    <DndProvider backend={HTML5Backend}>
      <BrowserRouter>
        <App />
      </BrowserRouter>
    </DndProvider>
  </React.StrictMode>
);
// If you want to start measuring performance in your app, pass a function
// to log results (for example: reportWebVitals(console.log))
// or send to an analytics endpoint. Learn more: https://bit.ly/CRA-vitals
reportWebVitals(() => {});