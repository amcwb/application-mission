from flask.json import jsonify
from backend import db
from backend.authenticate import gen_auth_token, is_authenticated
from backend.models.usertask import Task, User
from flask import Blueprint, current_app, render_template, request, session

users = Blueprint("users", __name__)


@users.route("/login", methods=["POST"])
def login():
    # If already logged in, just pass by
    if session["user"] is not None:
        return jsonify({}), 200
    
    email = request.form.get("email")
    password = request.form.get("password")

    user = User.query.filter_by(email=email).first()
    if user:
        # Check password
        if user.check_password(password):
            # Generate JWT
            return jsonify({
                "jwt": gen_auth_token(user.id)
            }), 200
        
        # Wrong password
        return jsonify({"error": "Incorrect details"}), 403
    
    # User not found
    return jsonify({"error": "That user does not exist"}), 400


@users.route("/create", methods=["POST"])
def create():
    name = request.form.get("name")
    surname = request.form.get("surname")
    email = request.form.get("email")
    password = request.form.get("password")
    confirm_password = request.form.get("confirm_password")

    # Make sure account doesn't exist already
    user = User.query.filter_by(email=email).first()
    if user:
        return jsonify({
            "error": "This account already exists"
        }), 403
    
    if password != confirm_password:
        return jsonify({
            "error": "These passwords do not match"
        }), 403

    user = User(
        name=name,
        surname=surname,
        email=email
    )

    user.set_password(password)

    db.session.add(user)
    db.session.commit()

    return jsonify({
        "user_id": user.id,
        "jwt": gen_auth_token(user.id)
    })
