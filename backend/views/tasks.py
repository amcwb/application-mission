from datetime import datetime
import json
from flask.json import jsonify
from backend import db
from backend.authenticate import is_authenticated
from backend.models.usertask import Task, User
from flask import Blueprint, current_app, render_template, request, session

tasks = Blueprint("tasks", __name__)


@is_authenticated
@tasks.route("/create", methods=["POST"])
def create():
    due = request.form.get("due", None)
    content = request.form.get("content")

    # We can assume session is populated as the user is authenticated
    author_id = session.get("user")

    task = Task(due=due, content=content, author_id=author_id)
    db.session.add(task)
    db.session.commit()
    
    return jsonify({
        "task_id": task.id
    }), 200


@is_authenticated
@tasks.route("/delete", methods=["POST"])
def delete():
    task_id = request.form.get("task_id")

    task = Task.query.filter_by(id=task_id).first()
    if task:
        db.session.delete(task)
        db.session.commit()
        
        return jsonify({}), 200
    else:
        return jsonify({}), 404


@is_authenticated
@tasks.route("/info", methods=["GET"])
def info():
    task_id = request.form.get("task_id")
    task = Task.query.filter_by(id=task_id).first()
    if task:
        return jsonify({
            "task_id": int(task_id),
            "due": task.due,
            "content": task.content,
            "author_id": task.author_id,
            "assigned_users": list(map(lambda p: p.id, task.assigned_users))
        }), 200
    else:
        return jsonify({}), 404

@is_authenticated
@tasks.route("/list", methods=["GET"])
def list_():
    tasks = Task.query.all()
    data_tasks = []
    for task in tasks:
        data_tasks.append({
            "task_id": int(task.id),
            "due": task.due,
            "content": task.content,
            "author_id": task.author_id,
            "assigned_users": list(map(lambda p: p.id, task.assigned_users))
        })
    
    return jsonify({"tasks": data_tasks}), 200


def _get_datetime_or_none(*options):
    for option in options:
        try:
            if isinstance(option, datetime):
                return option
            return datetime.fromtimestamp(option)
        except TypeError:
            pass
    
    return None


@is_authenticated
@tasks.route("/set_data", methods=["POST"])
def set_data():
    task_id = request.form.get("task_id")
    task = Task.query.filter_by(id=task_id).first()
    if task:
        try:
            data = json.loads(request.form.get("data"))
        except ValueError:
            return jsonify({
                "error": "Invalid data"
            }), 400
        
        task.due = _get_datetime_or_none(data.get("due"), task.due)
        task.content = data.get("content", task.content)
        assigned_users = data.get("assigned_users", map(lambda p: p.id, task.assigned_users))
        task.assigned_users = list(map(lambda p: User.query.filter_by(id=p).first(), assigned_users))

        db.session.commit()

        return jsonify({}), 200
    else:
        return jsonify({}), 404