import os
import shutil

BASE_DIR = r"c:\Users\Admin\Desktop\ConcursInfo\app\src\main"
JAVA_DIR = os.path.join(BASE_DIR, "java", "com", "example", "myapplication")
RES_DIR = os.path.join(BASE_DIR, "res")

SRC_PACKAGES = [
    "com.example.myapplication.model",
    "com.example.myapplication.models",
    "com.example.myapplication.ui.model"
]
DEST_PACKAGE = "com.example.myapplication.core.domain.model"
DEST_DIR = os.path.join(JAVA_DIR, "core", "domain", "model")

os.makedirs(DEST_DIR, exist_ok=True)

# 1. Rename QuestionModel in models to GameQuestionModel
models_qm_path = os.path.join(JAVA_DIR, "models", "QuestionModel.java")
new_models_qm_path = os.path.join(JAVA_DIR, "models", "GameQuestionModel.java")
if os.path.exists(models_qm_path):
    with open(models_qm_path, 'r', encoding='utf-8', errors='ignore') as f:
        content = f.read()
    content = content.replace("class QuestionModel", "class GameQuestionModel")
    content = content.replace("public QuestionModel(", "public GameQuestionModel(")
    content = content.replace("QuestionModel(", "GameQuestionModel(")
    with open(new_models_qm_path, 'w', encoding='utf-8') as f:
        f.write(content)
    os.remove(models_qm_path)


def execute_safely():
    # Find files using models.QuestionModel to replace references to GameQuestionModel BEFORE global replace
    # We must explicitly look for the old import
    for root, dirs, files in os.walk(JAVA_DIR):
        for file in files:
            if file.endswith('.java'):
                path = os.path.join(root, file)
                with open(path, 'r', encoding='utf-8', errors='ignore') as f:
                    content = f.read()
                
                if "import com.example.myapplication.models.QuestionModel;" in content:
                    content = content.replace("import com.example.myapplication.models.QuestionModel;", f"import {DEST_PACKAGE}.GameQuestionModel;")
                    content = content.replace("QuestionModel", "GameQuestionModel")
                    with open(path, 'w', encoding='utf-8') as f:
                        f.write(content)

execute_safely()

def move_files(src_dir_rel, pkg_name):
    src_dir = os.path.join(JAVA_DIR, *src_dir_rel.split('/'))
    if not os.path.exists(src_dir):
        return
    for root, dirs, files in os.walk(src_dir):
        for file in files:
            if file.endswith('.java'):
                src_path = os.path.join(root, file)
                dest_path = os.path.join(DEST_DIR, file)
                
                with open(src_path, 'r', encoding='utf-8', errors='ignore') as f:
                    content = f.read()
                
                # Replace package declaration
                # Also handle 'package com.example.myapplication.model.base;' if any
                if "model.base" in pkg_name and src_dir_rel.endswith("base"):
                    content = content.replace(f"package com.example.myapplication.model.base;", f"package {DEST_PACKAGE};")
                else:
                    content = content.replace(f"package {pkg_name};", f"package {DEST_PACKAGE};")
                
                with open(dest_path, 'w', encoding='utf-8') as f:
                    f.write(content)
                os.remove(src_path)
                print(f"Moved {file} to {DEST_PACKAGE}")

move_files("model/base", "com.example.myapplication.model.base")
move_files("model", "com.example.myapplication.model")
move_files("models", "com.example.myapplication.models")
move_files("ui/model", "com.example.myapplication.ui.model")

def replace_imports(dir_path, exts):
    for root, dirs, files in os.walk(dir_path):
        for file in files:
            if any(file.endswith(ext) for ext in exts):
                filepath = os.path.join(root, file)
                with open(filepath, 'r', encoding='utf-8', errors='ignore') as f:
                    content = f.read()
                
                new_content = content
                for src_pkg in SRC_PACKAGES:
                    new_content = new_content.replace(f"import {src_pkg}.*;", f"import {DEST_PACKAGE}.*;")
                    new_content = new_content.replace(f"import {src_pkg}.", f"import {DEST_PACKAGE}.")
                    new_content = new_content.replace(f"{src_pkg}.", f"{DEST_PACKAGE}.")
                
                # Cleanup specific leftover com.example.myapplication.model.base imports
                new_content = new_content.replace(f"import com.example.myapplication.model.base.", f"import {DEST_PACKAGE}.")
                
                if new_content != content:
                    with open(filepath, 'w', encoding='utf-8') as f:
                        f.write(new_content)
                    print(f"Updated imports in {file}")

replace_imports(JAVA_DIR, ['.java'])
replace_imports(RES_DIR, ['.xml', '.gradle'])

print("Refactoring complete.")
