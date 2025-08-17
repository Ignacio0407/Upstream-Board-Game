import { useState, useEffect, useRef } from 'react';
import { post, createWebSocket, createStompClient } from '../../util/fetchers.ts';
import tokenService from '../../services/token.service.ts';
import { ColorToRgb } from '../../util/ColorParser.ts';
import '../static/css/chat/chat.css'
import Match from '../../interfaces/Match.ts';
import Player from '../../interfaces/Player.ts';
import Message from '../../interfaces/Message.ts';

interface ChatPrompts {match:Match, players:Player[], currentPlayer:Player};

const Chat: React.FC<ChatPrompts> = ({match, players, currentPlayer}) => {
  const [messages, setMessages] = useState<Message[]>([]);
  const [newMessage, setNewMessage] = useState<string>("");
  const [isChatVisible, setIsChatVisible] = useState<boolean>(false); 
  const chatContainerRef = useRef<HTMLDivElement>(null);
  const jwt = tokenService.getLocalAccessToken();

  useEffect(() => {
    const socket = createWebSocket(); // move inside useEffect
    const stompClient = createStompClient(socket, jwt);
    stompClient.onConnect = () => {
      stompClient.subscribe(`/topic/chat/${match.id}`, (message) => {
        const newMessage = JSON.parse(message.body);
        setMessages((prev) => [...prev, newMessage]);
        scrollToBottom();
      });
    };
    stompClient.activate();
    return () => {stompClient.deactivate()};
  }, [match.id]);

  const handleSendMessage = async (e:React.FormEvent<HTMLFormElement>) => {
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

  const getPlayerColor = (playerId:number) => {
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

export default Chat;