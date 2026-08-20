import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../config/api-config';

export interface Contact {
  id: number;
  name: string;
  role: string;
  email: string;
  unreadCount?: number;
  lastMessageTime?: string;
}

export interface Message {
  id: number;
  senderId: number;
  senderName: string;
  senderRole: string;
  receiverId: number;
  receiverName: string;
  content: string;
  alert: boolean;
  timestamp: string;
}

@Injectable({
  providedIn: 'root'
})
export class ChatService {
  private apiUrl = API_BASE_URL + '/chat';

  constructor(private http: HttpClient) {}

  getContacts(): Observable<Contact[]> {
    return this.http.get<Contact[]>(`${this.apiUrl}/contacts`);
  }

  getConversation(contactId: number): Observable<Message[]> {
    return this.http.get<Message[]>(`${this.apiUrl}/messages/${contactId}`);
  }

  sendMessage(contactId: number, content: string, isAlert: boolean = false): Observable<Message> {
    return this.http.post<Message>(`${this.apiUrl}/messages/${contactId}`, { content, isAlert });
  }

  clearChat(contactId: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/messages/${contactId}`);
  }

  markAsRead(contactId: number): Observable<any> {
    return this.http.put(`${this.apiUrl}/messages/${contactId}/read`, {});
  }
}
