//The single-post.html page is one such situation, allowing users to click a button to upvote a post. These upvotes are tallied and saved in a vote_count column of the Post table in the MySQL database. When users click the upvote button, the upvote.js file will facilitate the processing necessary to increment the vote_count in the database.
async function upvoteClickHandler(event) {
  event.preventDefault();

//parse out the id via the window.location() method
  const id = window.location.toString().split('/')[
    window.location.toString().split('/').length - 1
  ];

//using response = await), it can capture the id value of the post and save it to a property (postId). So when the fetch() method makes a call to the /posts/upvote route, it captures the id of the location and then send it as a PUT request
  const response = await fetch('/posts/upvote', {
    method: 'PUT',
    body: JSON.stringify({
        postId: id
    }),
    headers: {
      'Content-Type': 'application/json'
    }
  });

  if (response.ok) {
    document.location.reload();
  } else {
    alert(response.statusText);
  }
}
//document.querySelector() attaches an event listener to the button, using class upvote-btn, and awaits a click.
document.querySelector('.upvote-btn').addEventListener('click', upvoteClickHandler);