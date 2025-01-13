import React, { useState, useEffect, useRef } from 'react';
import { patch } from '../util/fetchers';
import tokenService from '../services/token.service';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { ColorToRgb } from '../util/ColorParser';

const Chat = ({ match, players, currentPlayer }) => {
  const [messages, setMessages] = useState([]);
  const [newMessage, setNewMessage] = useState('');
  const chatContainerRef = useRef(null);
  const jwt = tokenService.getLocalAccessToken();
  const socket = new SockJS('http://localhost:8080/ws-upstream');
  const stompClient = new Client({
    webSocketFactory: () => socket,
    connectHeaders: {
      Authorization: `Bearer ${jwt}`,
    },
    debug: (str) => {
      console.log(str);
    },
    onStompError: (frame) => {
      console.error('Broker reported error: ' + frame.headers['message']);
      console.error('Additional details: ' + frame.body);
    },
    onWebSocketError: (error) => {
      console.error('Error with WebSocket:', error);
    },
  });

  useEffect(() => {
    stompClient.onConnect = () => {
      console.log('Connected to WebSocket for Chat');
      stompClient.subscribe(`/topic/chat/${match.id}`, (message) => {
        const newMessage = JSON.parse(message.body);
        setMessages((prev) => [...prev, newMessage]);
        scrollToBottom();
      });
    };

    stompClient.activate();

    return () => stompClient.deactivate();
  }, [match.id]);

  const handleSendMessage = async (e) => {
    e.preventDefault();
    if (!newMessage.trim()) return;

    try {
      const messagePayload = {
        content: newMessage,
        player: currentPlayer,
        match: { id: match.id },
      };

      const response = await patch('api/v1/messages', jwt, messagePayload);
      if (!response.ok) throw new Error('Error sending message');
      setNewMessage('');
    } catch (error) {
      console.error('Error sending message:', error);
    }
  };

  const scrollToBottom = () => {
    if (chatContainerRef.current) {
      chatContainerRef.current.scrollTop = chatContainerRef.current.scrollHeight;
    }
  };

  const getPlayerColor = (playerId) => {
    const player = players.find((p) => p.id === playerId);
    return player ? ColorToRgb(player.color) : '#000000';
  };

  return (
    <div className="chat-container" style={{
      position: 'fixed',
      left: '20px',
      bottom: '20px',
      width: '300px',
      height: '400px',
      backgroundColor: 'white',
      border: '1px solid #ccc',
      borderRadius: '8px',
      boxShadow: '0 2px 10px rgba(0,0,0,0.1)',
      zIndex: 1000,
      display: 'flex',
      flexDirection: 'column',
    }}>
      <div className="chat-header" style={{
        padding: '10px',
        borderBottom: '1px solid #eee',
        backgroundColor: '#f8f9fa',
        borderRadius: '8px 8px 0 0',
      }}>
        <h3 style={{ margin: 0, fontSize: '16px' }}>Chat de la partida</h3>
      </div>

      <div className="chat-messages" ref={chatContainerRef} style={{
        flex: 1,
        overflowY: 'auto',
        padding: '10px',
        display: 'flex',
        flexDirection: 'column',
        gap: '8px',
      }}>
        {messages.map((message) => (
          <div 
            key={message.id} 
            style={{
              alignSelf: message.player.id === currentPlayer.id ? 'flex-end' : 'flex-start',
              maxWidth: '80%',
            }}
          >
            <div style={{
              backgroundColor: message.player.id === currentPlayer.id ? '#007bff' : '#f1f1f1',
              color: message.player.id === currentPlayer.id ? 'white' : 'black',
              padding: '8px 12px',
              borderRadius: '12px',
              position: 'relative',
              boxShadow: `0 1px 2px ${getPlayerColor(message.player.id)}40`,
            }}>
              <div style={{
                fontSize: '12px',
                fontWeight: 'bold',
                marginBottom: '4px',
                color: message.player.id === currentPlayer.id ? 'white' : getPlayerColor(message.player.id),
              }}>
                {message.player.user.username}
              </div>
              <div>{message.content}</div>
            </div>
          </div>
        ))}
      </div>
      
      <form onSubmit={handleSendMessage} style={{
        padding: '10px',
        borderTop: '1px solid #eee',
        display: 'flex',
        gap: '8px',
      }}>
        <input
          type="text"
          value={newMessage}
          onChange={(e) => setNewMessage(e.target.value)}
          style={{
            flex: 1,
            padding: '8px',
            border: '1px solid #ddd',
            borderRadius: '4px',
            fontSize: '14px',
          }}
          placeholder="Escribe un mensaje..."
        />
        <button 
          type="submit"
          style={{
            padding: '8px 16px',
            backgroundColor: '#007bff',
            color: 'white',
            border: 'none',
            borderRadius: '4px',
            cursor: 'pointer',
          }}
        >
          Enviar
        </button>
      </form>
    </div>
  );
};

export default Chat;