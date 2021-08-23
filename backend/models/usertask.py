import hashlib
import os

from sqlalchemy.orm import relationship

from backend import db


users_tasks = db.Table(
    "users_tasks",
    db.metadata,
    db.Column("user_id", db.Integer, db.ForeignKey("users.id")),
    db.Column("task_id", db.Integer, db.ForeignKey("tasks.id")),
)


class User(db.Model):
    """
    User class that manages users and passwords
    """
    __tablename__ = "users"
    
    id = db.Column(db.Integer, primary_key=True)
    name = db.Column(db.String)
    surname = db.Column(db.String)
    email = db.Column(db.String)
    password = db.Column(db.LargeBinary)
    
    assigned_tasks = relationship("Task", secondary=users_tasks)

    def set_password(self, password: str = None):
        if password is None:
            self.password = None
        else:
            salt = os.urandom(32)
            key = hashlib.pbkdf2_hmac("sha256", password.encode("utf-8"), salt, 100000)

            self.password = salt + key
    
    def check_password(self, password: str) -> bool:
        salt = self.password[:32]
        key = self.password[32:]

        new_key = hashlib.pbkdf2_hmac("sha256", password.encode("utf-8"), salt, 100000)
        
        return key == new_key



class Task(db.Model):
    __tablename__ = "tasks"
    
    id = db.Column(db.Integer, primary_key=True)
    due = db.Column(db.DateTime)
    content = db.Column(db.String)
    
    author_id = db.Column(db.Integer, db.ForeignKey("users.id"))
    author = relationship("User", secondary=author_id)

    assigned_users = relationship("User", secondary=users_tasks)