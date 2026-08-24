const fs = require('fs');

let content = fs.readFileSync('src/app/pages/admin-students/admin-students.ts', 'utf8');

const oldLogic = `          this.successMessage = 'Student deleted successfully.';
          this.closeDeleteConfirmModal();

          setTimeout(() => {
            this.successMessage = '';
            this.cdr.markForCheck();
          }, 3000);`;

const newLogic = `          this.successMessage = 'Student deleted successfully.';
          this.closeDeleteConfirmModal();
          this.cdr.detectChanges(); // IMPORTANT: Instantly update UI

          setTimeout(() => {
            this.successMessage = '';
            this.cdr.detectChanges();
          }, 3000);`;

content = content.replace(oldLogic, newLogic);
fs.writeFileSync('src/app/pages/admin-students/admin-students.ts', content);

let content2 = fs.readFileSync('src/app/pages/admin-teachers/admin-teachers.ts', 'utf8');
content2 = content2.replace(`          this.successMessage = 'Teacher deleted successfully.';
          this.closeDeleteConfirmModal();

          setTimeout(() => {
            this.successMessage = '';
            this.cdr.markForCheck();
          }, 3000);`, `          this.successMessage = 'Teacher deleted successfully.';
          this.closeDeleteConfirmModal();
          this.cdr.detectChanges(); // IMPORTANT: Instantly update UI

          setTimeout(() => {
            this.successMessage = '';
            this.cdr.detectChanges();
          }, 3000);`);
fs.writeFileSync('src/app/pages/admin-teachers/admin-teachers.ts', content2);
