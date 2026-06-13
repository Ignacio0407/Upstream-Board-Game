/**
 * -----------------------------------------------------------------------------
 * Autor / Author: Ignacio Gutiérrez Serrera
 * 
 * 🇬🇧 This software was developed by Ignacio Gutiérrez Serrera.
 * It may be used, modified, and distributed for personal or commercial 
 * purposes, provided that explicit credit is given to the original author.
 * 
 * If this code is used as-is or with minor to moderate modifications 
 * as part of another software, proper attribution must be given to 
 * Ignacio Gutiérrez Serrera.
 * (e.g., credits, documentation, or user interface).
 * 
 * This license notice must not be removed in any redistribution 
 * of the source code or binaries.
 * 
 * 🇪🇸 Este software ha sido desarrollado por Ignacio Gutiérrez Serrera.
 * Se permite su uso, modificación y distribución con fines personales o 
 * comerciales, siempre que se mantenga el reconocimiento explícito al autor.
 * 
 * Si este código se utiliza tal cual, o con modificaciones leves o medias 
 * como parte de otro software, se debe otorgar el crédito correspondiente 
 * a Ignacio Gutiérrez Serrera.
 * (por ejemplo, en los créditos, documentación o interfaz del producto).
 * 
 * No se permite eliminar este aviso de licencia en ninguna redistribución 
 * del código fuente o binario.
 * -----------------------------------------------------------------------------
 */

import { useState, useEffect, useRef } from 'react';
import { post } from '../../util/fetchers.ts';
import tokenService from '../../services/token.service.ts';
import { ColorToRgb } from '../../util/ColorParser.ts';
import './chat.css'
import Match from '../../interfaces/Match.ts';
import Player from '../../interfaces/Player.ts';
import Message from '../../interfaces/Message.ts';
import WebSocketManager from '../../util/webSocketManager.ts';

interface ChatPrompts {match:Match, players:Player[], currentPlayer:Player};

const Chat: React.FC<ChatPrompts> = ({match, players, currentPlayer}) => {
  const [messages, setMessages] = useState<Message[]>([]);
  const [newMessage, setNewMessage] = useState<string>("");
  const [isChatVisible, setIsChatVisible] = useState<boolean>(false); 
  const chatContainerRef = useRef<HTMLDivElement>(null);
  const jwt = tokenService.getLocalAccessToken();

  useEffect(() => {
    const wsManager = new WebSocketManager(jwt);
        wsManager.initConnection();

    wsManager.addSubscription<Message>(`chat/${match.id}`, (message) => {
      setMessages((prev) => [...prev, message]);
      scrollToBottom();
    });
    return () => { wsManager.deactivate() };
  }, [match.id]);

  const handleSendMessage = async (e:React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (!newMessage.trim()) return;
    try {
      const playerId = currentPlayer.id;
      const playerName = currentPlayer.name;
      const matchId = match.id
      const content = newMessage;
      await post('/api/v1/messages/send', jwt, {playerId, playerName, matchId, content});
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
            {messages.map((message) => (
              <div key={message.id} className={`chat-message ${message.playerId === currentPlayer.id ? 'own-message' : ''}`}>
                <div className="message-content">
                  <span className="message-username" style={{ color: getPlayerColor(message.playerId) }}>
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