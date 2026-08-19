import { Component, OnInit, OnDestroy, ViewChild, ElementRef, AfterViewChecked, Output, EventEmitter } from '@angular/core';
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
  showClearConfirm = false;

  @ViewChild('messagesContainer') private messagesContainer!: ElementRef;

  constructor(private chatService: ChatService, private authService: AuthService) {
    this.currentUser = this.authService.getCurrentUser();
  }

  ngOnInit(): void {
    this.loadContacts();
  }

  ngOnDestroy(): void {
    this.stopPolling();
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
    }
    this.chatToggled.emit(this.isOpen);
  }

  closeChat(): void {
    if (this.isOpen) {
      this.isOpen = false;
      this.stopPolling();
      this.selectedContact = null;
      this.chatToggled.emit(this.isOpen);
    }
  }

  loadContacts(): void {
    this.chatService.getContacts().subscribe(res => {
      this.contacts = res;
    });
  }

  selectContact(contact: Contact): void {
    this.selectedContact = contact;
    this.loadMessages();
    this.startPolling();
  }

  loadMessages(): void {
    if (!this.selectedContact) return;
    this.chatService.getConversation(this.selectedContact.id).subscribe(res => {
      this.messages = res;
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

  sendMessage(): void {
    if (!this.selectedContact || !this.newMessage.trim()) return;
    
    // Students cannot reply to Admin
    if (this.currentUser.role === 'STUDENT' && this.selectedContact.role === 'ADMIN') {
      alert('You cannot reply to Admin alerts.');
      return;
    }

    this.chatService.sendMessage(this.selectedContact.id, this.newMessage).subscribe(() => {
      this.newMessage = '';
      this.loadMessages();
    });
  }

  clearChat(): void {
    if (!this.selectedContact) return;
    this.showClearConfirm = true;
  }

  confirmClearChat(): void {
    if (!this.selectedContact) return;
    this.chatService.clearChat(this.selectedContact.id).subscribe(() => {
      this.messages = [];
      this.showClearConfirm = false;
    });
  }

  cancelClearChat(): void {
    this.showClearConfirm = false;
  }

  private scrollToBottom(): void {
    try {
      this.messagesContainer.nativeElement.scrollTop = this.messagesContainer.nativeElement.scrollHeight;
    } catch(err) { }
  }
}
