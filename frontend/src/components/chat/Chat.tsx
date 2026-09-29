/**
 * -----------------------------------------------------------------------------
 * Autor / Author: Ignacio Gutiérrez Serrera
 * GitHub: https://github.com/Ignacio0407 (@Ignacio0407)
 * Contact / Email: ignaciogutierrezserrera@gmail.com
 * 
 * 🇬🇧 NON-COMMERCIAL & EDUCATIONAL LICENSE:
 * This software was developed by Ignacio Gutiérrez Serrera.
 * Permission is granted to use, copy, modify, and study this software for 
 * non-commercial, personal, academic, and educational purposes, provided 
 * that this copyright notice, author attribution, and contact details are 
 * preserved in all copies or substantial portions of the software.
 * 
 * Any commercial use, integration into proprietary software, or closed-source 
 * distribution is strictly prohibited without prior written permission and an 
 * express commercial license granted by the author.
 * 
 * 🇪🇸 LICENCIA PARA FINES NO LUCRATIVOS Y EDUCATIVOS:
 * Este software ha sido desarrollado por Ignacio Gutiérrez Serrera.
 * Se autoriza su uso, copia, modificación y estudio exclusivamente para 
 * fines personales, académicos, de investigación y educativos no lucrativos, 
 * con la condición obligatoria de mantener intacto este aviso de copyright, 
 * la atribución de autoría y los datos de contacto en todas las copias.
 * 
 * Queda terminantemente prohibido cualquier uso comercial, integración en 
 * software privativo o distribución con fines lucrativos sin la autorización 
 * previa por escrito del autor y la adquisición de una licencia comercial.
 * -----------------------------------------------------------------------------
 */

import { useState, useEffect, useRef } from 'react';
import { post } from '../../util/fetchers.ts';
import tokenService from '../../services/token.service.ts';
import { ColorToRgb } from '../../lobby/ColorParser.ts';
import './chat.css'
import Match from '../../interfaces/Match.ts';
import Player from '../../interfaces/Player.ts';
import Message from '../../interfaces/Message.ts';
import WebSocketManager from '../webSocketManager/webSocketManager.ts';

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

  const handleSendMessage = async (e: React.SyntheticEvent<HTMLFormElement>) => {
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
      <button type="button" className="chat-tab" onClick={() => setIsChatVisible((prev) => !prev)}>
        Chat
      </button>

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