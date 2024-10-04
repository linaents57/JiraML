# Install dependencies first
# pip install flask sentence-transformers scikit-learn

from flask import Flask, request, jsonify
from sentence_transformers import SentenceTransformer
from sklearn.metrics.pairwise import cosine_similarity
import numpy as np

app = Flask(__name__)

# Load the pre-trained BERT model
model = SentenceTransformer('bert-base-nli-mean-tokens')

@app.route('/find-best-match', methods=['POST'])
def find_best_match():
    data = request.json
    dataset_questions = data['dataset_questions']
    user_question = data['user_question']

    # Generate embeddings
    dataset_embeddings = model.encode(dataset_questions)
    user_embedding = model.encode([user_question])

    # Calculate cosine similarity between the user's question and each dataset question
    similarities = cosine_similarity(user_embedding, dataset_embeddings)

    # Find the index of the highest similarity
    best_match_idx = np.argmax(similarities)

    # Return the index of the best match
    return jsonify({'best_match_index': int(best_match_idx)})

if __name__ == '__main__':
    app.run(debug=True)
