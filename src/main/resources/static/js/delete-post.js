//allows users to click a button to delete a post. The post that will be deleted is determined by the postId. When a user clicks the Delete Post button, the delete-post.js file will be called to facilitate the deletion of the Post object from the corresponding table in the database.
async function deleteFormHandler(event) {
  event.preventDefault();

  const id = window.location.toString().split('/')[
    window.location.toString().split('/').length - 1
  ];

  const response = await fetch(`/api/posts/${id}`, {
    method: 'DELETE'
  });

  if (response.ok) {
    document.location.replace('/dashboard/')
  } else {
    alert(response.statusText);
  }
}

document.querySelector('.delete-post-btn').addEventListener('click', deleteFormHandler);