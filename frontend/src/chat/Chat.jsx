import { useState, useEffect, useRef } from 'react';
import { get, post } from '../util/fetchers';
import tokenService from '../services/token.service';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { ColorToRgb } from '../util/ColorParser';
import '../static/css/chat/chat.css'

export default function Chat({ match, players, currentPlayer }) {
  const [messages, setMessages] = useState([]);
  const [newMessage, setNewMessage] = useState('');
  const [isChatVisible, setIsChatVisible] = useState(false); 
  const chatContainerRef = useRef(null);
  const jwt = tokenService.getLocalAccessToken();

  const socket = new SockJS('http://localhost:8080/ws-upstream');
  
  const stompClient = new Client({
    webSocketFactory: () => socket,
    connectHeaders: {
      Authorization: `Bearer ${jwt}`,
    },
    debug: (str) => console.log(str),
    onStompError: (frame) => console.error('Broker reported error: ' + frame.headers['message']),
    onWebSocketError: (error) => console.error('Error with WebSocket:', error),
  });

  useEffect(() => {
    stompClient.onConnect = () => {
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
      const playerId = currentPlayer.id;
      const playerName = currentPlayer.name;
      const matchId = match.id
      const content = newMessage;
      await post('/api/v1/messages', jwt, {playerId, playerName, matchId, content});
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
    <div className="chat-wrapper">
      <div className="chat-tab" onClick={() => setIsChatVisible((prev) => !prev)}>
        Chat
      </div>

      {isChatVisible && (
        <div className="chat-container">
          <div className="chat-header">
            <h3>Match Chat</h3>
          </div>

          <div className="chat-messages" ref={chatContainerRef}>
            {messages && messages.map((message) => (
              <div
                key={message.id}
                className={`chat-message ${
                  message.playerId === currentPlayer.id ? 'own-message' : ''
                }`}
              >
                <div className="message-content">
                  <span
                    className="message-username"
                    style={{ color: getPlayerColor(message.playerId) }}
                  >
                    {message.playerName}
                  </span>
                  <p>{message.content}</p>
                </div>
              </div>
            ))}
          </div>

          <form className="chat-input" onSubmit={handleSendMessage}>
            <input
              type="text"
              value={newMessage}
              onChange={(e) => setNewMessage(e.target.value)}
              placeholder="Write a message..."
            />
            <button type="submit">Send</button>
          </form>
        </div>
      )}
    </div>
  );
};