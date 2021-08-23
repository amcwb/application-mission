from sqlalchemy.ext.declarative import declarative_base

# Declare base
Base = declarative_base()

from .usertask import User, Task, users_tasks