import { Component, OnInit, OnDestroy, ViewChild, ElementRef, AfterViewChecked, Output, EventEmitter, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ChatService, Contact, Message } from '../../../core/services/chat.service';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-chat-widget',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './chat-widget.component.html',
  styleUrls: ['./chat-widget.component.css']
})
export class ChatWidgetComponent implements OnInit, OnDestroy, AfterViewChecked {
  @Output() chatToggled = new EventEmitter<boolean>();
  isOpen = false;
  contacts: Contact[] = [];
  selectedContact: Contact | null = null;
  messages: Message[] = [];
  newMessage = '';
  currentUser: any;
  pollingInterval: any;
  contactsPollingInterval: any;
  showClearConfirm = false;
  totalUnreadCount = 0;
  activeMenuContactId: number | null = null;

  @ViewChild('messagesContainer') private messagesContainer!: ElementRef;

  constructor(private chatService: ChatService, private authService: AuthService, private cdr: ChangeDetectorRef) {
    this.authService.getCurrentUser().subscribe(user => {
      this.currentUser = user;
    });
  }

  ngOnInit(): void {
    this.startContactsPolling();
  }

  ngOnDestroy(): void {
    this.stopPolling();
    this.stopContactsPolling();
  }

  ngAfterViewChecked(): void {
    this.scrollToBottom();
  }

  toggleChat(): void {
    this.isOpen = !this.isOpen;
    if (this.isOpen) {
      this.loadContacts();
    } else {
      this.stopPolling();
      this.selectedContact = null;
      this.messages = [];
    }
    this.chatToggled.emit(this.isOpen);
  }

  closeChat(): void {
    if (this.isOpen) {
      this.isOpen = false;
      this.stopPolling();
      this.selectedContact = null;
      this.messages = [];
      this.chatToggled.emit(this.isOpen);
    }
  }

  loadContacts(): void {
    this.chatService.getContacts().subscribe(res => {
      this.contacts = res;
      this.totalUnreadCount = this.contacts.reduce((acc, contact) => acc + (contact.unreadCount || 0), 0);
      this.cdr.detectChanges();
    });
  }

  selectContact(contact: Contact | null): void {
    this.selectedContact = contact;
    this.messages = [];
    if (contact) {
      this.loadMessages();
      this.startPolling();
    } else {
      this.stopPolling();
      this.loadContacts();
    }
  }

  loadMessages(): void {
    if (!this.selectedContact) return;
    this.chatService.getConversation(this.selectedContact.id).subscribe(res => {
      this.messages = res;
      this.cdr.detectChanges();
    });
  }

  startPolling(): void {
    this.stopPolling();
    this.pollingInterval = setInterval(() => {
      this.loadMessages();
    }, 5000);
  }

  stopPolling(): void {
    if (this.pollingInterval) {
      clearInterval(this.pollingInterval);
    }
  }

  startContactsPolling(): void {
    this.loadContacts();
    this.contactsPollingInterval = setInterval(() => {
      this.loadContacts();
    }, 10000);
  }

  stopContactsPolling(): void {
    if (this.contactsPollingInterval) {
      clearInterval(this.contactsPollingInterval);
    }
  }

  sendMessage(): void {
    if (!this.selectedContact || !this.newMessage.trim()) return;
    
    // Users cannot reply to Admin
    if (this.selectedContact.role === 'ADMIN') {
      alert('You cannot reply to Admin alerts.');
      return;
    }

    this.chatService.sendMessage(this.selectedContact.id, this.newMessage).subscribe(() => {
      this.newMessage = '';
      this.loadMessages();
      this.loadContacts();
    });
  }

  clearChat(): void {
    if (!this.selectedContact) return;
    this.showClearConfirm = true;
  }

  confirmClearChat(): void {
    if (!this.selectedContact) return;
    const contactId = this.selectedContact.id;
    this.messages = [];
    this.showClearConfirm = false;
    this.chatService.clearChat(contactId).subscribe();
  }

  cancelClearChat(): void {
    this.showClearConfirm = false;
  }

  toggleMenu(event: Event, contactId: number): void {
    event.stopPropagation();
    this.activeMenuContactId = this.activeMenuContactId === contactId ? null : contactId;
  }

  closeMenu(): void {
    this.activeMenuContactId = null;
  }

  markAsReadFromMenu(event: Event, contact: Contact): void {
    event.stopPropagation();
    this.activeMenuContactId = null;
    this.chatService.markAsRead(contact.id).subscribe(() => {
      contact.unreadCount = 0;
      this.totalUnreadCount = this.contacts.reduce((acc, c) => acc + (c.unreadCount || 0), 0);
      this.cdr.detectChanges();
    });
  }

  clearChatFromMenu(event: Event, contact: Contact): void {
    event.stopPropagation();
    this.activeMenuContactId = null;
    
    // Clear instantly if this is the active chat
    if (this.selectedContact && this.selectedContact.id === contact.id) {
      this.messages = [];
    }
    
    this.chatService.clearChat(contact.id).subscribe(() => {
      // Chat cleared in backend
    });
  }

  showDateHeader(index: number): boolean {
    if (index === 0) return true;
    const currentMsgDate = new Date(this.messages[index].timestamp).toDateString();
    const prevMsgDate = new Date(this.messages[index - 1].timestamp).toDateString();
    return currentMsgDate !== prevMsgDate;
  }

  getDateHeaderText(timestamp: string): string {
    const msgDate = new Date(timestamp);
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    
    const msgDay = new Date(msgDate);
    msgDay.setHours(0, 0, 0, 0);
    
    const diffTime = Math.abs(today.getTime() - msgDay.getTime());
    const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24));
    
    if (diffDays === 0) {
      return 'Today';
    } else if (diffDays === 1) {
      return 'Yesterday';
    } else if (diffDays < 7) {
      const days = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];
      return days[msgDate.getDay()];
    } else {
      const options: Intl.DateTimeFormatOptions = { weekday: 'short', day: 'numeric', month: 'short' };
      return msgDate.toLocaleDateString('en-US', options);
    }
  }

  getContactTimeText(timestamp: string): string {
    if (!timestamp) return '';
    const msgDate = new Date(timestamp);
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    
    const msgDay = new Date(msgDate);
    msgDay.setHours(0, 0, 0, 0);
    
    const diffTime = Math.abs(today.getTime() - msgDay.getTime());
    const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24));
    
    if (diffDays === 0) {
      return msgDate.toLocaleTimeString('en-US', { hour: 'numeric', minute: '2-digit', hour12: true });
    } else if (diffDays === 1) {
      return 'Yesterday';
    } else if (diffDays < 7) {
      const days = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];
      return days[msgDate.getDay()];
    } else {
      return msgDate.toLocaleDateString('en-GB', { day: '2-digit', month: '2-digit', year: '2-digit' }).replace(/\//g, '/');
    }
  }

  private scrollToBottom(): void {
    try {
      this.messagesContainer.nativeElement.scrollTop = this.messagesContainer.nativeElement.scrollHeight;
    } catch(err) { }
  }
}
