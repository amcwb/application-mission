from flask.json import jsonify
from backend import db
from backend.authenticate import is_authenticated
from backend.models.usertask import Task
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
